package io.github.gaming32.fungame.entity

import io.github.gaming32.fungame.World
import org.ode4j.math.DVector3
import org.ode4j.ode.OdeHelper

class PlayerEntity(world: World) : Entity(world, OdeHelper.createCapsule(world.space, 0.5, 1.8)) {
    companion object {
        val START_POS = DVector3(0.0, 1.4, -5.0)
    }

    init {
        body.position = START_POS
    }

    override fun kill() {
        body.position = START_POS
        body.linearVel = DVector3.ZERO
    }
}
