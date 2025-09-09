package mod_classes;

import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.game.Level;

class JumpingMonster extends Monster {
	@Override
	public void tick(Level level, float delta)
	{
		super.tick(level, delta);

		// Might be dead?
		if(!isActive)
			return;

		// Hop!
		if(isOnFloor) {
			za += 0.05;
		}

	}
}