package thread;

import java.awt.Color;
import java.util.Map;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.JSlider;
import javax.sound.midi.*; // IMPORT MIDI INDISPENSABLE
import Sound.SoundPlayer;

// On n'implémente plus Runnable, mais Receiver 
public class Runnable_Sequencer implements Receiver {

	private boolean running;
	private JSlider sliderTempo;
	private Map<String, JToggleButton[]> mapGrille;
	private Map<String, SoundPlayer> mapSons;
	
	private Sequencer horlogeMidi;
	private int stepActuel = 0;
	private int tailleTotaleGrille;
	
	public Runnable_Sequencer(Map<String, JToggleButton[]> mapGrille, Map<String, SoundPlayer> mapSons, JSlider sliderTempo) {
		this.mapGrille = mapGrille;
		this.mapSons = mapSons;
		this.sliderTempo = sliderTempo;
		this.tailleTotaleGrille = this.mapGrille.get("Kick").length;
		this.running = false;
	}
	
	// Remplace le run() classique
	public void lancerSequenceur() {
		try {
			this.horlogeMidi = MidiSystem.getSequencer(false); // false = séquenceur silencieux
			this.horlogeMidi.open();
			this.horlogeMidi.getTransmitter().setReceiver(this); // On branche l'horloge sur NOTRE classe

			Sequence seq = new Sequence(Sequence.PPQ, 4); // 4 ticks par noire = 1 tick par case
			Track track = seq.createTrack();

			// On dessine une piste MIDI de la taille exacte de notre grille
			for (int i = 0; i < this.tailleTotaleGrille; i++) {
				ShortMessage msg = new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100);
				track.add(new MidiEvent(msg, i));
			}
			// On place un événement "Note Off" silencieux à la position 16 pour forcer la durée totale
			track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), this.tailleTotaleGrille));
			// Configuration et lancement
			this.horlogeMidi.setSequence(seq);
			this.horlogeMidi.setLoopCount(Sequencer.LOOP_CONTINUOUSLY); // Boucle infinie
			this.horlogeMidi.setTempoInBPM(this.sliderTempo.getValue());
			
			this.stepActuel = 0;
			this.running = true;
			this.horlogeMidi.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		this.running = false;
		if (this.horlogeMidi != null && this.horlogeMidi.isOpen()) {
			this.horlogeMidi.stop();
			this.horlogeMidi.close();
		}
		
		// Nettoyage visuel à l'arrêt
		SwingUtilities.invokeLater(() -> {
			for (JToggleButton[] boutons : mapGrille.values()) {
				for (int i = 0; i < boutons.length; i++) {
					boutons[i].setBackground(Color.WHITE);
				}
			}
		});
	}

	// =======================================================
	//  Appelé automatiquement par le MIDI
	// =======================================================
	@Override
	public void send(MidiMessage message, long timeStamp) {
		if (!this.running) return;

		if (message instanceof ShortMessage) {
			ShortMessage sm = (ShortMessage) message;
			
			// Si le MIDI envoie un "tic" (une note)
			if (sm.getCommand() == ShortMessage.NOTE_ON) {
				
				final int step = this.stepActuel;
				final int stepPrecedent = (step == 0) ? tailleTotaleGrille - 1 : step - 1;

				// 1. Mise à jour visuelle (Jaune/Orange)
				SwingUtilities.invokeLater(() -> {
					for (JToggleButton[] boutons : mapGrille.values()) {
						boutons[stepPrecedent].setBackground(Color.WHITE);
						if (boutons[step].isSelected()) {
							boutons[step].setBackground(Color.ORANGE); 
						} else {
							boutons[step].setBackground(Color.YELLOW); 
						}
					}
				});

				// 2. Lecture Audio
				for (Map.Entry<String, JToggleButton[]> ligne : this.mapGrille.entrySet()) {
					if (ligne.getValue()[step].isSelected()) {
						SoundPlayer lecteur = this.mapSons.get(ligne.getKey());
						if (lecteur != null) {
							lecteur.play(); 
						}
					}
				}

				// 3. Mise à jour du BPM en temps réel (si on bouge le slider)
				this.horlogeMidi.setTempoInBPM(this.sliderTempo.getValue());

				// 4. On avance d'une case !
				this.stepActuel = (this.stepActuel + 1) % this.tailleTotaleGrille;
			}
		}
	}

	@Override
	public void close() {} // Requis par l'interface Receiver
}