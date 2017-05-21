# ranta

# k‰yt‰n samoja liikkeit‰ kuin ohjattavaan ukkoon (ne on ladattu UP, DOWN jne animaatioihin)
setAnimation("HERMO_JUOPPO", "UP", "DOWN", "LEFT", "RIGHT");
#aseta reitti
setPath("HERMO_JUOPPO", "juoppo", 1);

#spe:ss‰ asetetut kutsut
# $jarvi
# $viinipullo

int huku=0
#jos k‰velee j‰rveen
func jarvi()

	if huku==0 
		huku=1;
	endif

	if huku==2
		# k‰ynnist‰ peli alusta
		loadRoom("ranta", "alku");
	endif

endfunc

func viinipullo()
	
	# sit‰ ei voi ottaa ellei tied‰ ett‰ raivo haluaa viini‰
	if action["RAIVO"]==0
		message(15, -1, -1, "Tuossa pullossa onkin jotain jeje..mutta kaverini pit‰‰ siit‰ kiinni enk‰ halua varastaa kaverilta¥ju.");
	endif

	if action["RAIVO"]==1 && action["PUTELI"]==0
		message(15, -1, -1, "Je, kaverini on sammuneena. Otankin putelin parempaan talteen, jejuje.");
		getItem("viini");  # ota pullo (h‰vitt‰‰ sen ruudulta, siirt‰‰ tavaroihin)
		action["PUTELI"]=1;
	endif

endfunc


# pakolliset funktiot
#kutsutaan joka framella
func update()
	updateAnimation("HERMO_JUOPPO");
	updateAnimation("EGO");

endfunc

#kutsutaan kun on aika piirt‰‰
func render()
    drawAnimImage("HERMO_JUOPPO", 0);
    drawAnimImage("EGO", 5);

	# kun palataan ruutuun kaljakorin kanssa:
	if action["KALJAT"]==2
		message(25, -1, -1, "\"TƒSSƒ HIEMAN AAMUPALAA JUJE!\" huusit juopoille. \"Jeje, kiitoksia yst‰v‰ hyv‰. Ota toki itsekin jejuje, kun jaksoit sen korin hakea. Ja onhan sit‰ tietenkin lis‰‰ jeje.\" sanoi joku pulsu. \"Juje, totta helvet*ss‰ otan!!\" sanoit.");
	endif
	if action["KALJAT"]==3
		message(25, -1, -1, "N‰in alkoi hieno maanantai p‰iv‰ kes‰lomalla. Ja juopot alkoi laulamaan jujejuje laulua. Peli loppu!");
	endif
	if action["KALJAT"]==4
		endGame();
	endif
	if action["KALJAT"]>0
		action["KALJAT"]=action["KALJAT"]+1;
	endif


	if huku==1
		# fontsize=40,  x ja y -1 niin keskitt‰‰ x ja y suunnassa
		message(40,-1, -1, "Jeje, voi pahus, k‰velit j‰rveen. Olisit muistanut ettet osaa uida selvinp‰in ju!");
		huku=2;
	endif

endfunc
