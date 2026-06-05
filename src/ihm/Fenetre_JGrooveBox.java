	package ihm;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map; 

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
//import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** pour events sliders***/
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.sound.sampled.FloatControl;

import Sound.SoundPlayer; // import class du player audio

import thread.Runnable_Sequencer; // import runnable


public class Fenetre_JGrooveBox extends JFrame implements ActionListener , ChangeListener{

	// composant border layout 1 nord
	private JLabel lbl_titre;
	
	// composant border layout2 , au nord
	private JPanel panelTempo;
    private JLabel lblTempo;
    private JSlider sliderTempo;
    private JLabel lblSignature;
    private JLabel lblGrid; 
    private JButton btnPlay;
    private JButton btnStop;
    private JButton btnImporter;
    private JButton btnSauvegarder;
    
    // pour la grille
    private String[] INSTRUMENTS = {"Open Hi-hat", "Closed Hi-hat", "Clap", "Snare", "Kick"};
    private int NB_TEMPS = 16;
    
 // Notre collection pour mémoriser la grille : associer nom instrument et sa ligne de boutons cochés ou non
    private Map<String, JToggleButton[]> mapGrille;
    
    private SoundPlayer lecteurAudio;
    
   // private String snare_chemin;
    
    private Runnable_Sequencer sequencer; //runnable
    
    // map qui associe sons a son sound player
    //Elle relie le nom d'un instrument au fichier audio prêt à être joué en mémoire vive.
    private Map<String, SoundPlayer> mapSonsToPlay;
	
	public Fenetre_JGrooveBox (String titre){
		super(titre);
		this.init(); 
		this.build();
		this.decorate();
		
		this.add_listeners();
	}
	
	private void init()
	{
		this.setSize(800,600);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.lbl_titre = new JLabel(this.getTitle(), JLabel.CENTER);
		// Instanciation des contrôles du haut
        this.lblTempo = new JLabel("Tempo = 120 BPM", JLabel.CENTER);
        this.sliderTempo = new JSlider(50, 150, 120);
        
        // On regroupe le texte du tempo et le slider dans un petit panel pour faciliter le placement
        this.panelTempo = new JPanel(new BorderLayout());
        this.panelTempo.add(lblTempo, BorderLayout.NORTH);
        this.panelTempo.add(sliderTempo, BorderLayout.CENTER);
        this.panelTempo.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // border ligne grise sur panel temp
        
        this.lblSignature = new JLabel("Signature\n 4/4", JLabel.CENTER);
        this.lblSignature.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        this.lblGrid = new JLabel("Grid : 1/16", JLabel.CENTER);
        this.lblGrid.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.btnPlay = new JButton("►");
        this.btnStop = new JButton("Stop");
        this.btnImporter = new JButton("Importer dernier pattern sauvegardé");
        this.btnSauvegarder = new JButton("Sauvegarder pattern actuel");
        
        this.mapGrille = new LinkedHashMap<>();
        
        this.mapSonsToPlay = new HashMap<>();
        
        //this.snare_chemin="sounds/snare.wav";
        try {
        	// On remplit le dictionnaire avec les 5 lecteurs audio.
        	this.mapSonsToPlay.put("Open Hi-hat", new SoundPlayer("sounds/open_hat.wav"));
            this.mapSonsToPlay.put("Closed Hi-hat", new SoundPlayer("sounds/closed_hat.wav"));
            this.mapSonsToPlay.put("Clap", new SoundPlayer("sounds/clap.wav"));
            this.mapSonsToPlay.put("Snare", new SoundPlayer("sounds/snare.wav"));
            this.mapSonsToPlay.put("Kick", new SoundPlayer("sounds/kick.wav"));
            // On instancie le lecteur avec le chemin du fichier 
            /*this.lecteurAudio = new SoundPlayer(snare_chemin); */
        } catch (Exception e) {
            System.err.println("Erreur au chargement des sons ! Vérifier chemins des fichiers");
            e.printStackTrace(); // Affiche l'erreur
        }
	}
	
	private void decorate() {
        // Couleurs et polices de base
        this.getContentPane().setBackground(new Color(225, 240, 255));
        
        this.lbl_titre.setFont(new Font("Arial", Font.BOLD, 30));
        this.lbl_titre.setOpaque(true);
        this.lbl_titre.setBackground(new Color(200, 220, 250));
        this.lbl_titre.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        this.btnPlay.setBackground(new Color(120, 200, 120)); // Vert
        this.btnPlay.setFont(new Font("Arial", Font.BOLD, 30));
        
        this.btnStop.setBackground(new Color(250, 100, 100)); // Rouge
        this.btnStop.setFont(new Font("Arial", Font.BOLD, 18));
        
        this.btnImporter.setBackground(new Color(255, 235, 200));
        this.btnImporter.setFont(new Font("Arial", Font.BOLD, 14));
        this.btnSauvegarder.setBackground(new Color(255, 235, 200));
        this.btnSauvegarder.setFont(new Font("Arial", Font.BOLD, 14));
        
        this.panelTempo.setBackground(new Color(255, 235, 200));
        this.panelTempo.setFont(new Font("Arial", Font.BOLD, 16));
        this.lblSignature.setOpaque(true);
        this.lblSignature.setFont(new Font("Arial", Font.BOLD, 14));
        this.lblSignature.setBackground(new Color(255, 235, 200));
        this.lblGrid.setOpaque(true);
        this.lblGrid.setFont(new Font("Arial", Font.BOLD, 14));
        this.lblGrid.setBackground(new Color(255, 235, 200));
    }
	
	private void build()
	{
		// main panel
		JPanel panneau_principal = new JPanel(new BorderLayout());
		
		
		// panel interface (composants interactifs au nord et au centre grille
		JPanel zone_interface = new JPanel (new BorderLayout());
		zone_interface.setOpaque(false);
		
		// zone boutons north du panel zone interface
		JPanel panneau_boutons = new JPanel(new GridBagLayout());
		panneau_boutons.setOpaque(false);
		
		
		
		//**** remplissage gridbag "panneau bouton *********
		GridBagConstraints gbcTop = new GridBagConstraints();
        
        // Insets : marges (haut, gauche, bas, droite) pour aérer les composants
        gbcTop.insets = new Insets(10, 10, 10, 10); 
        gbcTop.fill = GridBagConstraints.BOTH; // Les composants s'étirent pour remplir leur case

        // LIGNE 0
        // --- Tempo ---
        gbcTop.gridx = 0; gbcTop.gridy = 0;  // on le place en haut a gauche
        gbcTop.gridwidth = 2; // FUSION : Le tempo prend 2 colonnes de large !
        gbcTop.gridheight = 1;
        panneau_boutons.add(panelTempo, gbcTop);

       // --- Bouton Play ---
        gbcTop.gridx = 2; gbcTop.gridy = 0;
        gbcTop.gridwidth = 1; // On réinitialise à 1 colonne
        gbcTop.gridheight = 2; // FUSION : Play prend 2 lignes de hauteur !
        panneau_boutons.add(this.btnPlay, gbcTop);

        // --- Bouton Stop ---
        gbcTop.gridx = 3; gbcTop.gridy = 0;
        gbcTop.gridheight = 2; // FUSION : Stop prend aussi 2 lignes de hauteur !
        panneau_boutons.add(this.btnStop, gbcTop);

        // --- Bouton Importer ---
        gbcTop.gridx = 4; gbcTop.gridy = 0;
        gbcTop.gridheight = 1; // On réinitialise la hauteur à 1
        panneau_boutons.add(this.btnImporter, gbcTop);


        // LIGNE 1
        // --- Signature ---
        gbcTop.gridx = 0; gbcTop.gridy = 1;
        gbcTop.gridwidth = 1;
        panneau_boutons.add(this.lblSignature, gbcTop);

        // --- Grid 1/16 ---
        gbcTop.gridx = 1; gbcTop.gridy = 1;
        panneau_boutons.add(this.lblGrid, gbcTop);

        // Note : x=2 et x=3 sont occupés par Play et Stop qui descendent de la ligne 0
        
        // --- Bouton Sauvegarder ---
        gbcTop.gridx = 4; gbcTop.gridy = 1;
        panneau_boutons.add(this.btnSauvegarder, gbcTop);
		
        
        
        
		// zone grille au centre de la zone interface
		JPanel panneauGrille = new JPanel(new GridBagLayout());
        panneauGrille.setOpaque(false);
        panneauGrille.setBackground(new Color(190, 225, 255));
        panneauGrille.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
    	//**** remplissage gridbag  grille centrale *********
        
        GridBagConstraints gbcGrid = new GridBagConstraints();
        gbcGrid.fill = GridBagConstraints.BOTH; 
        gbcGrid.weightx = 1.0; // Permet aux cases de s'étirer horizontalement
        gbcGrid.weighty = 1.0; // Permet aux cases de s'étirer verticalement

        
        
        // --- LIGNE 0 : Les en-têtes (1, 1.2, 1.3, 1.4) ---
        gbcGrid.gridy = 0;
        
        // Case vide en haut à gauche (au-dessus des noms d'instruments)
        gbcGrid.gridx = 0; gbcGrid.gridwidth = 1;
        panneauGrille.add(creerLabel("Samples"), gbcGrid);

        gbcGrid.gridx = 1;
        panneauGrille.add(creerLabel("Volume"), gbcGrid);
        
        // Les blocs de temps (1, 1.2, 1.3, 1.4) : FUSION DE 4 COLONNES
        String[] Temps = {"1", "1.2", "1.3", "1.4"};
        int colonneActuelle = 2; // On commence à la colonne 1 (juste après l'instrument)
        
        for (String texte : Temps) {
            gbcGrid.gridx = colonneActuelle;
            gbcGrid.gridwidth = 4; // FUSION : Ce label couvre 4 cases de la grille !
            panneauGrille.add(creerLabel(texte), gbcGrid);
            colonneActuelle += 4; // On avance de 4 colonnes pour le prochain label
        }
        
        
     // --- LIGNES SUIVANTES : Les instruments ---
        gbcGrid.gridwidth = 1; // On remet la largeur à 1 case par défaut

        for (int i = 0; i < INSTRUMENTS.length; i++) {
            gbcGrid.gridy = i + 1; // i+1 car la ligne 0 est l'en-tête

            // 1. Nom de l'instrument
            gbcGrid.gridx = 0;
            JLabel lbl_nom_Instru = creerLabel(INSTRUMENTS[i]);
            panneauGrille.add(lbl_nom_Instru, gbcGrid);

            // ligne de slider de volume pour chaque colonne de samples
            gbcGrid.gridx = 1;
            gbcGrid.weightx = 0.0;
            JSlider sliderVol = new JSlider(0, 100, 80);
            sliderVol.setPreferredSize(new Dimension(60, 20)); // Largeur max de 60 pixels
            sliderVol.setOpaque(false);
            sliderVol.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            // 1. On donne un nom au slider (ex: "Kick", "Snare")
            sliderVol.setName(INSTRUMENTS[i]); 
            // 2. On lui dit que cette fenêtre écoute ses changements
            sliderVol.addChangeListener(this);
            panneauGrille.add(sliderVol, gbcGrid);
            
            // CRÉATION D'UN TABLEAU POUR STOCKER LES 16 BOUTONS DE CETTE LIGNE/instrument
            JToggleButton[] ligneBoutons = new JToggleButton[NB_TEMPS];
            
            // 2. Les 16 cases à cocher (Temps) - Le slider a été retiré
            gbcGrid.weightx = 1.0;
            for (int t = 0; t < NB_TEMPS; t++) {
                gbcGrid.gridx = 2 + t; // On commence directement après l'instru(0)
                
                JToggleButton case_sequencer = new JToggleButton();
                
                case_sequencer.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                case_sequencer.setBackground(Color.WHITE); // Case blanche par défaut
                
                panneauGrille.add(case_sequencer, gbcGrid);
                
             // ON SAUVEGARDE LE BOUTON DANS NOTRE TABLEAU
                ligneBoutons[t] = case_sequencer;
            }
            
            // on range chaque "ligne" de boutons, associé a son instrument, dans la MaP
            this.mapGrille.put(INSTRUMENTS[i], ligneBoutons);
        }
		
        
        // On remplit le layout "zone interface"
		zone_interface.add(panneau_boutons,BorderLayout.NORTH);
		zone_interface.add(panneauGrille,BorderLayout.CENTER);
		
		panneau_principal.add(lbl_titre,BorderLayout.PAGE_START);
		panneau_principal.add(zone_interface, BorderLayout.CENTER);
		this.setContentPane(panneau_principal);
	}
	
	private JLabel creerLabel(String texte) {
        JLabel label = new JLabel(texte, JLabel.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        label.setOpaque(true);
        return label;
    }
	
	private void add_listeners()
	{
		this.btnSauvegarder.addActionListener(this); // ecoute le clic sur bouton sauvegarder
		this.btnPlay.addActionListener(this);
		this.btnImporter.addActionListener(this);
		this.btnStop.addActionListener(this);
		this.sliderTempo.addChangeListener(this);
	}

	
	
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == this.btnSauvegarder)
		{
			System.out.println("Clic sur sauvegarder");
			sauvegarderPATTERN();
		}
		
		if (event.getSource() == this.btnImporter)
		{
			System.out.println("Clic sur importer");
			importerPATTERN();
		}
		
		if (event.getSource() == this.btnPlay) {
	        System.out.println("Lecture lancée !");
	        
	     // On ne lance le séquenceur que s'il n'y en a pas déjà un en cours
	        if (this.sequencer == null) {
	            this.sequencer = new Runnable_Sequencer(this.mapGrille, this.mapSonsToPlay, this.sliderTempo);
	            
	            // NOUVELLE FAÇON DE LANCER : Plus besoin de "new Thread()" !
	            this.sequencer.lancerSequenceur(); 
	        }
	        
	        // On ne lance un Thread que s'il n'y en a pas déjà un en cours
	        /*if (this.sequencer == null) {
	            // On lui passe notre grille et nos sons
	            this.sequencer = new Runnable_Sequencer(this.mapGrille, this.mapSonsToPlay,this.sliderTempo);
	            Thread t = new Thread(this.sequencer);
	            t.start(); 
	        }*/
	       /* // On s'assure que le lecteur a bien été initialisé pour éviter un crash (NullPointerException)
	        if (this.lecteurAudio != null) {
	            this.lecteurAudio.play(); // Lance la méthode clip.start() en arrière-plan
	        }*/
	    }
		
		if (event.getSource() == this.btnStop) {
	        System.out.println("Arrêt de la lecture.");
	        
	        if (this.sequencer != null) {
	            this.sequencer.stop(); // Passe le booléen running à false
	            this.sequencer = null; // Libère la variable pour la prochaine lecture
	        }
	    }
	}
	
	
	/**** Ecoute les events des sliders , pour tempo et volume instrus****/
	@Override 
	public void stateChanged(ChangeEvent event) {
	    
	    //  slider du Tempo 
	    if (event.getSource() == this.sliderTempo) {
	        // On met à jour l'affichage du texte en direct
	        this.lblTempo.setText("Tempo = " + this.sliderTempo.getValue() + " BPM");
	    }
	    
	    // On vérifie d'abord que la source est bien un JSlider
	    else if (event.getSource() instanceof JSlider) {
	        
	        // On transforme la source générique en JSlider pour pouvoir lire ses données
	        JSlider sliderModifie = (JSlider) event.getSource();
	        
	        // On récupère le "nom secret" qu'on lui a donné dans build() (ex: "Kick")
	        String nomInstrument = sliderModifie.getName();
	        
	        // Si le slider a bien un nom (ce n'est donc pas le slider Tempo qui n'a pas de nom)
	        if (nomInstrument != null) {
	            
	            // 1. On lit la valeur (de 0 à 100)
	            int volumeActuel = sliderModifie.getValue();
	            
	            // 2. On récupère le bon lecteur audio dans notre dictionnaire
	            SoundPlayer lecteur = this.mapSonsToPlay.get(nomInstrument);
	            
	            // 3. Sécurité et application du volume
	            if (lecteur != null) {
	                lecteur.setVolume(volumeActuel); 
	            }
	        }
	    }
	}
	
	
	//FONCTION POUR SAUVEGARDER PATTERN APRES CLIC SUR BOUTON SAUVEGARDER
	private void sauvegarderPATTERN () {
		try 
		{
			File fichier_sauvegarde = new File("data/pattern_sauvegarde.txt");
			/*if (!fichier_sauvegarde.exists()) {
				fichier_sauvegarde.createNewFile();
			}*/
			
			System.out.println(fichier_sauvegarde);
	        
	        // Le paramètre "true" dans FileWriter permet d'ajouter à la suite du fichier (mode "Append"). 
	        FileWriter fw = new FileWriter(fichier_sauvegarde, true);
	        BufferedWriter bw = new BufferedWriter(fw);
	        
	        // 2. Création et écriture de l'en-tête avec la date
	        LocalDateTime dateActuelle = LocalDateTime.now();
	        DateTimeFormatter formateur = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	        
	        bw.write("=== Sauvegarde du " + dateActuelle.format(formateur) + " ===");
	        bw.newLine(); //saut de ligne
	        bw.newLine(); //saut de ligne
			
		// On parcourt chaque "ligne" (chaque Clé-Valeur) de la Map
			for (Map.Entry<String, JToggleButton[]> ligne_Map_entry: this.mapGrille.entrySet())
			{
				String nomInstrument = ligne_Map_entry.getKey(); // on recupere la cle : nom instru
		        JToggleButton[] Boutons_grille = ligne_Map_entry.getValue(); // on recupere les boutons
		        
		        // On prépare la ligne de texte pour le fichier
		        StringBuilder ligneTexte = new StringBuilder(nomInstrument + " : ");
		        
		        ligneTexte.append(" |1|  ");
		        
		        // On parcourt les 16 boutons pour voir s'ils sont cochés
		        for (int t = 0; t < Boutons_grille.length; t++) {
		            
		        	
		            if (Boutons_grille[t].isSelected()) {
		                ligneTexte.append("X "); // on ecrit "X" pour dire sample joué à "x" temps
		            } else {
		                ligneTexte.append("0 "); // Silence
		            }
		            
		         // On ajoute les séparateurs visuels tous les 4 temps
		            
		            if (t == 3) {
		                ligneTexte.append(" |1.2|  ");
		            } 
		            else if (t == 7) {
		                ligneTexte.append(" |1.3|  ");
		            } 
		            else if (t == 11) {
		                ligneTexte.append(" |1.4|  ");
		            }
		        }
		        
		        bw.write(ligneTexte.toString());  //on ecrit nos ligne de sauvegarde dans le fichier
	            bw.newLine();
		        // Affiche dans la console 
		        System.out.println(ligneTexte.toString());
		    }
			// saut de ligne final pour aérer entre deux clics "Sauvegarder"
	        bw.newLine(); 
	        bw.close();
		}
		catch (IOException e) 
		{
	        System.err.println("Erreur : Impossible d'écrire dans le fichier.");
	        e.printStackTrace();
		}
	}
	
	//FONCTION POUR Importer PATTERN APRES CLIC SUR BOUTON SAUVEGARDER
	private void importerPATTERN () 
	{
		File fichier_sauvegarde = new File("data/pattern_sauvegarde.txt");

	    // 1. on vérifie que le fichier existe avant d'essayer de l'ouvrir
	    if (!fichier_sauvegarde.exists()) {
	        System.err.println("Aucune sauvegarde trouvée ! Sauvegardez d'abord un pattern.");
	        return; // On arrête la méthode ici
	    }

	    try (BufferedReader br = new BufferedReader(new FileReader(fichier_sauvegarde))) {
	        
	        String ligne;
	       // On lit la ligne ET on vérifie qu'elle n'est pas nulle directement ici
	        while ((ligne = br.readLine()) != null)
			{
				// 3. On ignore les lignes d'en-tête (=== Sauvegarde...) et les lignes vides
	            // On ne traite que les lignes qui contiennent le séparateur " : "
	            if (ligne.contains(" : ")) 
	            {
	                
	                // On coupe la ligne en deux morceaux
	                String[] tokens = ligne.split(" : "); 
	                String nomInstrument = tokens[0].trim(); // Le morceau de gauche (ex: "Kick")
	                String patternTexte = tokens[1];         // Le morceau de droite (ex: " |1|  X 0 0 0..."
	                
	                
	             // 4. On vérifie si cet instrument existe bien dans notre grille visuelle
	                if (this.mapGrille.containsKey(nomInstrument)) {
	                    
	                    // On récupère le tableau des 16 boutons correspondant à cet instrument
	                    JToggleButton[] boutons = this.mapGrille.get(nomInstrument);
	                    
	                    int indexBouton = 0; // Pour savoir à quel bouton (de 0 à 15) on en est
	                    
	                    //  On parcourt la ligne de texte lettre par lettre !
	                    for (char caractere : patternTexte.toCharArray()) {
	                        
	                        if (caractere == 'X') {
	                            boutons[indexBouton].setSelected(true); // On coche la case
	                            indexBouton++; // On passe au bouton suivant
	                        } 
	                        else if (caractere == '0') {
	                            boutons[indexBouton].setSelected(false); // On décoche la case
	                            indexBouton++; // On passe au bouton suivant
	                        }
	                        
	                        //si on a rempli les 16 boutons, on arrête d'analyser cette ligne
	                        // (Cela permet d'ignorer les espaces ou les barres verticales "|")
	                        if (indexBouton >= NB_TEMPS) {
	                            break; 
	                        }
	                    }
	                }
				
	            }
			}
	         
		System.out.println("Importation du pattern réussie !");
		} catch (Exception e) 
	    {
			System.err.println("Erreur de lecture du fichier :");
			e.printStackTrace();
			// TODO: handle exception
		}
		
	}
	
}
	
