# mˆkki

#spe:ss‰ asetetut kutsut
# $raivohullu
# $kaljat

int x, y

# korjaa oven y-kohtaa ettei ukko mene sen taakse
setYP("ovi", 40);

 # jos yritet‰‰n avata ovi
func ovi()
	# jos raivo on hereill‰, ovelle ei p‰‰se
	if action["RAIVO"]<2
		message(15, -1, -1, "\"MUN OHI EI KULJE KETƒƒN, Mƒ VEDƒN SUA PATAAN!!\" karjui Raaka-Jorma.");
		action["RAIVO"]=1;
	endif

	# avaa ovi
	if action["RAIVO"]==2
			setItem("ovi", 0, 0, 1, 1);
	endif
endfunc

func raivohullu()

	# raivo on hereill‰
	if action["RAIVO"]<2
		message(15, -1, -1, "\"Tapellaaks?? TAPELLAAKS SAA*ANA?!?\", uhosi Raaka-Jorma.");
	endif

endfunc

# jos k‰velee mˆkkiin sis‰lle
func kaljat()

	if action["KALJAT"]==0

		message(15, -1, -1, "K‰vit hakemassa korillisen kaljaa. Jejejuje kohta saa k‰nnin p‰‰lle!");
		loadImage("kori.png", "KORI");
		action["ULOS"]=1;
		action["KALJAT"]=1;
	endif

endfunc


# pakolliset funktiot
#kutsutaan joka framella
func update()

	if action["ULOS"]==1
		action["ULOS"]=0;
		loadRoom("mokki", "ulkona"); # asetetaan ukko pihalle
	endif

	updateAnimation("EGO");


endfunc

#kutsutaan kun on aika piirt‰‰
func render()

    drawAnimImage("EGO", 5);

	if action["RAIVOLLEVIINI"]>40
		drawImage(120, 360, "SAMMUNUT", 0);

		# ota ukon koordinaatit ja laita kori sen syliin
        x=positionX["EGO"];
        y=positionY["EGO"];
        drawImage(x, y, "KORI", -20);
	endif


	if using("viini", "raivo", 60)=="1"

		if action["RAIVO"]<2 && action["RAIVOLLEVIINI"]==0
			message(15, -1, -1, "\"Viini‰? No kyll‰ kelpaa, kiitti saat*na!\n");
			inventory["viini"]="2";
			action["RAIVOLLEVIINI"]=1;
		endif

		# sammunut
		if action["RAIVO"]==2
			message(15, -1, -1, "Jeje, ‰l‰ her‰t‰ sit‰ koska se alkaa taas meuhkaamaan ja sit‰ ei pirukaan kest‰!");
		endif

	endif

	# tekstit viimeiseksi ettei muuta kr‰‰s‰‰ mene niitten p‰‰lle.
	# render() lopussa on hyv‰ paikka

	if action["RAIVOLLEVIINI"]>0
		action["RAIVOLLEVIINI"]=action["RAIVOLLEVIINI"]+1;

		if action["RAIVOLLEVIINI"]==60
			message(15, -1, -1, "\"*Glunk glunk* ‰‰‰h (rˆˆˆyh!)");
		endif
		if action["RAIVOLLEVIINI"]==100
			message(15, -1, -1, "\"V*tut t‰m‰ mit‰‰n viini‰ ole, mehupirtua aah, hyv‰‰ shilti!\n");
		endif
		if action["RAIVOLLEVIINI"]==120
			message(15, -1, -1, "*Glunk glunk* alkaa olo paranee shhaatana juje!");
		endif
		if action["RAIVOLLEVIINI"]==200
			message(15, -1, -1, "\"Ooeeevoo... ZZZzzz.. krooh pyyh..\"");
			action["RAIVO"]=2;

			# pit‰‰ muuttaa kuva
			# ( itemname,  toInventory,  toCursor,  removePoly,  removeItem)
			setItem("raivo", 0, 0, 1, 1);
			loadImage("raivo2.png", "SAMMUNUT");
		endif

		if action["RAIVOLLEVIINI"]==300
			message(15, -1, -1, "Raivo sammui.");
		endif


	endif



endfunc
