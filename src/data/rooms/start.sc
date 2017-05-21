# lataa ohjattavan ukon asennot
loadAnimation("UP");
loadAnimation("DOWN");
loadAnimation("LEFT");
loadAnimation("RIGHT");

# aseta animaatio (EGO on ohjattava ukko)
# parameters: mihin_objektiin, ylös, alas, vasen, oikea
setAnimation("EGO", "UP", "DOWN", "LEFT", "RIGHT");

# vakiokursorit, joiden toiminnallisuus ohjelmoitu java-ohjelmaan
loadCursor("cur_walk.png", "WALK");
loadCursor("cur_eye.png", "LOOK");
loadCursor("cur_get.png", "TAKE");


# lataa peli, aseta ukko alku-paikkaan
loadRoom("ranta", "alku");


# update() ja render() pitää löytyä joka scriptistä
func update()
endfunc

func render()
endfunc
