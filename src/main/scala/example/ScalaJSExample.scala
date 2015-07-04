package example
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.html

import objects._
import scala.math.{min, max}



@JSExport
object ScalaJSExample 
{
	def handlePlayerMovement(player : Actor, keys : collection.mutable.Set[Int], g : Game) =
	{
		if (keys(38)) player.moveLoc(0, -2, g)
	    if (keys(37)) player.moveLoc(-2, 0, g)
	    if (keys(39)) player.moveLoc(2, 0, g)
	    if (keys(40)) player.moveLoc(0, 2, g)
	}

	@JSExport
	def main(canvas: html.Canvas): Unit = 
	{
		dom.console.log("butts butts butts")
		val keysDown = collection.mutable.Set[Int]()
		val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

	
		//Make the game
		val g = new Game(GVs.GAMEX, GVs.GAMEY)

		//Make the player
		//val player = 
		//g.addActor(player)

		//Make the map
		g.genMap()

		//MAKEA THA ZOMBIE
		val zed = new Zombie(GVs.GAMEX / 2 - 10, GVs.GAMEY - 50, 20, g.player)
		g.addActor(zed)

		//make a hooman
		val hooman = new Human(g.player.locX + 50, g.player.locY, 50)
		g.addActor(hooman)

		//make a wall
		val wall = new Wall(200, 400, 200, 10)
		g.addObj(wall)

		val zedSpawner = new ZombieSpawner(GVs.GAMEX / 2 - 10, GVs.GAMEY - 50, 20)
		g.addActor(zedSpawner)


		def clear() = 
		{
			ctx.fillStyle = s"rgb(200, 200, 200)"
			ctx.fillRect(0, 0, GVs.FULLX, GVs.FULLY)
		}

		def run()
		{
			//bump score
			g.score += 1

			//Check if the game is over
			if(g.player.hp <= 0)
			{
				//it's over
				ctx.font = "75px sans-serif"
				ctx.fillStyle = "white"
				ctx.fillText("It's over!", GVs.GAMEX/2, GVs.GAMEY/2)
			}
			else
			{
				//dom.console.log("py: " + g.player.locY)
				//Check if the player has moved to the next map
				if(g.player.locY < 0)
				{
					//score boost!
					g.score += g.difficulty * 1000

					dom.console.log("Generating new map")

					//save actors because genMap deletes them
					val oldActs = g.acts.clone

					//Load new map
					g.genMap()

					dom.console.log("Copying " + oldActs.size + " actors")

					//Add all the actors as delays
					for(a <- oldActs)
					{
						if(a != g.player)
						{
							a match
							{
								case delayed : DelayedActor =>
									delayed.time += GVs.GAMEY / delayed.speed
									g.addActor(delayed)
								case _ =>
									val time = a.locY / a.speed

									//Move them to valid spots
									a.locY = GVs.GAMEY //all the way down
									a.locX = max(a.locX, GVs.GAMEX / 2 - 50) //minimum right they can be
									a.locX = min(a.locX, GVs.GAMEX/2 + 50 - a.sizeX)

									a.moveToNewMap(g)

									val delayedAct = new DelayedActor(a, time)

									g.addActor(delayedAct)
							}
						}
					}
				}
				//handle player movement
				handlePlayerMovement(g.player, keysDown, g)

				//Now handle all other ais
				g.runAllAIs()

				//Clear the screen
				clear()

				//Draw health bars and such

				//health
				ctx.fillStyle = s"rgb(200, 0, 0)"
				ctx.fillRect(GVs.GAMEX, 0, max((GVs.FULLX - GVs.GAMEX) * g.player.hp * 1.0/g.player.maxHp, 0), 80)
				ctx.fillStyle = "black"
				ctx.font = "12px sans-serif"
				ctx.fillText("health", GVs.GAMEX, 10)

				//score!
				ctx.fillStyle = "black"
				ctx.font = "12px sans-serif"
				ctx.fillText("score", GVs.GAMEX, 90)
				ctx.font = "50px sans-serif"
				ctx.fillText(g.score.toString, GVs.GAMEX, 130)
				

				//Draw the map
				g.drawAll(ctx)
			}
		}

		clear()


		// dom.onkeypress = {(e: dom.KeyboardEvent) =>
	 //      if (e.keyCode.toInt == 32) bullets = player +: bullets
	 //    }
	    dom.onkeydown = {(e: dom.KeyboardEvent) =>
	      keysDown.add(e.keyCode.toInt)
	    }
	    
	    dom.onkeyup = {(e: dom.KeyboardEvent) =>
	      keysDown.remove(e.keyCode.toInt)
	    }


		dom.setInterval(() => run, 20)
	}
}
