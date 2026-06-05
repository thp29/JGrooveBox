package Sound;

import java.io.File;
import javax.sound.sampled.*;

public class SoundPlayer {
	
	private Clip[] clips;
	private int indexActuel = 0;
	private final int POLYPHONIE = 4; // 4 voix simultanées par instrument !
	
	public SoundPlayer(String filename) throws Exception {
		clips = new Clip[POLYPHONIE];
		
		// On charge le même fichier audio dans 4 lecteurs différents
		for (int i = 0; i < POLYPHONIE; i++) {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filename));
			clips[i] = AudioSystem.getClip();
			clips[i].open(audioStream);
		}
	}

	public void play() {
		// On prend le lecteur actuel
		Clip c = clips[indexActuel];
		c.stop();
		c.setMicrosecondPosition(0);
		c.start();
		
		// On décale l'index pour que le prochain coup utilise le lecteur suivant
		indexActuel = (indexActuel + 1) % POLYPHONIE; 
	}
	
	public void stop() {
		// On arrête tous les lecteurs d'un coup
		for (Clip c : clips) {
			c.stop();
			c.setMicrosecondPosition(0);
		}
	}
	
	// Applique le volume à TOUS les lecteurs du pool
	public void setVolume(int pourcentage) {
		for (Clip c : clips) {
			if (c != null && c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
				if (pourcentage <= 0) {
					gainControl.setValue(gainControl.getMinimum()); 
				} else {
					float dB = (float) (Math.log10(pourcentage / 100.0) * 20.0);
					gainControl.setValue(dB);
				}
			} 
			
		}
	}
}