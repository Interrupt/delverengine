package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.items.Bow;
import com.interrupt.dungeoneer.entities.items.ItemModification;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;
import java.util.Random;

public class Item extends Entity {

	public enum ItemType { key, torch, potion, wand, sword, ring, amulet, junk, armor, quest, scroll, bow, thrown, stack, gold };
	/** Item type */
	public ItemType itemType;

	public enum ItemCondition { broken, worn, normal, fine, excellent };

	private static String[] itemConditionText = {
		"Broken",
		"Worn",
		"Normal",
		"Fine",
		"Excellent"
	};

	public static Float[] itemConditionCostMod = {
			0.5f,
			0.75f,
			1.0f,
			1.2f,
			1.5f
	};

	/** Is item a pickup? */
	@EditorProperty(group = "Pickup")
	public boolean isPickup = false;

	/** Distance at which to perform pickup behavior. */
	@EditorProperty(group = "Pickup")
	public float pickupDistance = 1.0f;

	/** How much bob does floating pickup have? */
	@EditorProperty(group = "Pickup")
	public float bobAmplitude = 0.05f;

	/** Current item condition */
	@EditorProperty
	public ItemCondition itemCondition = ItemCondition.normal;

	/** Item enchantment */
	public ItemModification enchantment = null;

	/** Item prefx enchantment */
	public ItemModification prefixEnchantment = null;

	/** Sprite index of item while held */
	@EditorProperty
	public Integer heldTex = null;

	/** Sprite index of item while in inventory */
	@EditorProperty
	public Integer inventoryTex = null;

	/** Slot to equip item to */
	@EditorProperty
	public String equipLoc = "";

	/** Sound played when Item is equipped */
	@EditorProperty
	public String equipSound = "";

	/** Sound played when Item is picked up */
	@EditorProperty
	public String pickupSound = "pu_gen.mp3";

	/** Is item identified? */
	@EditorProperty
	public boolean identified = true;

	/** Amount of gold item is worth */
	public int cost = 20;

	protected transient Vector3 workVec = new Vector3();

	/** Item's level. Will scale stats */
	public int itemLevel = 1;

	/** Minimum allowed item level */
    @EditorProperty(type = "Level Scaling")
    public Integer minItemLevel = null;

    /** Maximum allowed item level */
    @EditorProperty(type = "Level Scaling")
    public Integer maxItemLevel = null;

    /** Is item unique? */
	@EditorProperty
	public boolean unique = false;

	/** Entity id to send trigger event when picked up */
	@EditorProperty(type = "Triggers")
	public String triggersOnPickup = null;

	@EditorProperty( group = "Visual - Model", type = "FILE_PICKER", params = "meshes")
	public String meshFile = null;

	@EditorProperty( group = "Visual - Model", type = "FILE_PICKER", params = "meshes")
	public String viewMeshFile = null;

	@EditorProperty( group = "Visual - Model", type = "FILE_PICKER", params = "")
	public String textureFile = "";

	/** Allow enchantments on item when spawned? */
	@EditorProperty(group = "Spawning")
	public boolean canSpawnEnchanted = true;

	/** Set random condition for item when spawned? */
	@EditorProperty(group = "Spawning")
	public boolean randomizeCondition = true;

	public boolean wasOnEntity = true;

	public transient String lastMeshFile = null;
	public transient String lastTextureFile = null;

	private static final transient Entity pickupHelper = new Entity();

	public Item() {
		isSolid = true;
		artType = ArtType.item;
		spriteAtlas = "item";
		type = EntityType.item;
		isOnFloor = false;
		wasOnFloorLast = false;
		collision.set(0.2f,0.2f,0.2f);
		stepHeight = 0.1f;
		canSleep = true;
		shadowType = ShadowType.BLOB;
	}

	/** Item name */
	@EditorProperty
	public String name;

	/** Item description */
	@EditorProperty
	public String description;

	public String getDisplayName() {
		try {
			return StringManager.get(this.name);
		}
		catch (Exception ex) {}

		return this.name;
	}

	public Item(float x, float y, int tex, ItemType itemType, String name)
	{
		super(x, y, tex, true);
		artType = ArtType.item;
		isSolid = true;
		type = EntityType.item;

		this.itemType = itemType;
		spriteAtlas = "item";
		this.name = name;

		canSleep = true;
	}

	@Override
	public void init(Level level, Level.Source source) {
		super.init(level, source);

		if(source == Level.Source.LEVEL_START) {
			if (this instanceof Bow) {
				roll = 135;
				if(drawable instanceof DrawableSprite)
					((DrawableSprite) drawable).drawOffset.z = 0.0f;
			} else if (this instanceof Weapon) {
				roll = 45f;
				if(drawable instanceof DrawableSprite)
					((DrawableSprite) drawable).drawOffset.z -= 0.15f;
			} else {
				roll = 0f;
			}
		}
	}

	@Override
	public void encroached(Entity hit)
	{
		float speed = workVec.len();
		if(speed > 0.1f) {
			bonkEntity(hit, speed);
		}
	}

	@Override
	public void encroached(Player hit)
	{
		if (isPickup && canPickup(hit)) {
			doPickup(hit);
			return;
		}

		if(!ignorePlayerCollision) {
			float speed = workVec.len();
			if(speed > 0.06f) {
				bonkEntity(hit, speed);
			}
		}

		if(Game.isMobile)
			Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Item.mobileUseMessageText"),GetInfoText()));

		if(!ignorePlayerCollision) {
			ignorePlayerCollision = true;
		}
	}

	// Called when an item is moving fast enough to damage an entity
	public void bonkEntity(Entity hit, float speed) {
		if(hit == null) return;

		hit.hit(0, 0, 1 + Game.instance.player.getDamageStatBoost(), 0, DamageType.PHYSICAL, this);
		if(hit.isDynamic) {
			xa = 0;
			ya = 0;
		}

		if(hit instanceof Player) {
			ignorePlayerCollision = true;
			Audio.playSound("hit.mp3,hit_02.mp3,hit_03.mp3,hit_04.mp3", speed * 6f);
		}
	}

	public void use(Player player, float projx, float projy)
	{
		float pxdir = player.x - x;
		float pydir = player.y - y;
		float playerdist = GlRenderer.FastSqrt(pxdir * pxdir + pydir * pydir);

		if(playerdist > 1.1) return;

		pickup(player);
	}

	public boolean inventoryUse(Player player){
		//Override this and set to true when item can be used in inventory
        return false;
	}

	public void tossItem(Level level, float attackPower) {
		// Override this
	}

	/**
	 * Predicate for determining if a <code>doPickup(player)</code> can be performed. Can be overridden
	 * to handle special cases. E.g. not picking up a health item if at full heath.
	 * Overriding methods should still call this via <code>super.doPickup(player)</code>.
	 *
	 * @param player The player instance.
	 * @return True if pickup can be performed.
	 */
	public boolean canPickup(Player player) {
		return player.hasFreeInventorySpace();
	}

	/**
	 * Default behavior for a pickup. Overriding methods should still
	 * call this via <code>super.doPickup(player)</code>.
	 *
	 * @param player The player instance.
	 */
	public void doPickup(Player player) {
		pickup(player);
	}


	protected void pickup(Player player)
	{
		if(Math.abs(xa) >= 0.01f || Math.abs(ya) >= 0.01f || Math.abs(za) >= 0.01f) return;

		if(Game.instance.player.addToInventory(this))
		{
			isActive = false;

			makeItemPickupAnimation(player);

			// reset some variables that aren't needed when in the inventory (save file space)
			x = 0;
			y = 0;
			isOnFloor = false;
			wasOnFloorLast = false;
			resetTickCount();

			Audio.playSound(pickupSound, 0.3f, 1f);

			if(triggersOnPickup != null && !triggersOnPickup.isEmpty()) {
				Game.instance.level.trigger(this, triggersOnPickup, name);
			}
		}
		else {
			Game.ShowMessage(StringManager.get("entities.Item.noRoomText"), 1.0f, 1f);
		}
	}

	transient static Vector3 t_itemPickupDir = new Vector3();
	protected void makeItemPickupAnimation(Player player) {

		t_itemPickupDir.set(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 2.8f, 0.0001f);
		GameManager.renderer.camera.unproject(t_itemPickupDir);
		t_itemPickupDir.sub(x, z + yOffset - 0.15f, y).scl(0.15f);

		// Make pickup effect
		Particle p = CachePools.getParticle();
		p.lifetime = 8;
		p.x = x;
		p.y = y;
		p.z = z + yOffset;
		p.setSpriteAtlas(spriteAtlas);
		p.tex = tex;
		p.checkCollision = false;
		p.floating = true;
		p.fullbrite = fullbrite;
		p.collision.set(collision);
		p.shadowType = shadowType;
		p.roll = roll;
		p.scale = scale;
		p.shader = shader;
		p.xa = t_itemPickupDir.x;
		p.ya = t_itemPickupDir.z;
		p.za = t_itemPickupDir.y;
		Game.instance.level.SpawnNonCollidingEntity(p);
	}

	@Override
	public void tick(Level level, float delta)
	{
		workVec.set(xa, ya, za);
		collidesWith = (workVec.len() > 0.1f) ? CollidesWith.all : CollidesWith.nonActors;

		super.tick(level, delta);

		if(!isOnFloor && !isOnEntity && !wasOnEntity) {
			if(floating) {
				yOffset = (float)Math.sin(GlRenderer.time) * 0.05f;
			}
			else {
				roll += 250f * (delta * 0.015f);
			}
		}
		else {
			if (this instanceof Bow) {
				roll = Interpolation.linear.apply(roll, 135, 0.3f);

				if(drawable instanceof DrawableSprite)
					((DrawableSprite) drawable).drawOffset.z = 0.0f;
			} else if (this instanceof Weapon) {
				roll = Interpolation.linear.apply(roll, 45, 0.3f);

				if(drawable instanceof DrawableSprite)
					((DrawableSprite) drawable).drawOffset.z -= 0.15f;
			}
			else {
				roll = Interpolation.linear.apply(roll, 0, 0.3f);
			}
		}

		if (isPickup) {
			if (floating) {
				yOffset = (float) Math.cos(2 * GlRenderer.time + (x + y) * Math.PI) * bobAmplitude;
			}

			if (floating && drawable instanceof DrawableMesh) {
				DrawableMesh dm = (DrawableMesh)drawable;
				dm.rotZ = GlRenderer.time * (360f / 2f);
			}

			pickupHelper.x = x;
			pickupHelper.y = y + yOffset;
			pickupHelper.z = z;
			pickupHelper.collidesWith = CollidesWith.actorsOnly;
			pickupHelper.collision.x = pickupDistance / 2f;
			pickupHelper.collision.y = pickupDistance / 2;
			pickupHelper.collision.z = pickupDistance;

			Array<Entity> encroaching = level.getEntitiesColliding(x, y, z, pickupHelper);

			for (Entity e : encroaching) {
				if (e instanceof Player) {
					Player p = (Player)e;
					encroached(p);
					break;
				}
			}
		}

		// make solid after spawning
		if(isOnFloor) isSolid = true;
		wasOnEntity = isOnEntity;
	}

	public void tickEquipped(Player player, Level level, float delta, String equipLoc) {
		// only tick some attachments when held offhand, not all
		if(attached != null) {

			// let attachments preserve their offsets
			if(attachmentTransform == null) attachmentTransform = new Vector3(0,0,0);

			for(int i = 0; i < attached.size; i++) {
				Entity attachment = attached.get(i);
				attachment.x += x - attachmentTransform.x;
				attachment.y += y - attachmentTransform.y;
				attachment.z += z - attachmentTransform.z;
				attachment.owner = this;
				attachment.isSolid = false;	// attachments are always non solid

				if(attachment instanceof DynamicLight) {
					DynamicLight light = (DynamicLight)attachment;
					light.updateLightColor(delta);
					player.torchColor.r += light.workColor.x;
					player.torchColor.g += light.workColor.y;
					player.torchColor.b += light.workColor.z;
					player.visiblityMod += Math.max(Math.max(light.workColor.x, light.workColor.y), light.workColor.z);
				}
				else {
					attachment.tick(level, delta);
				}
			}

			attachmentTransform.set(x,y,z);
		}
	}

	String t_name = new String();
	public String GetName() {
		String prefixEnchantmentText = "";
		String conditionText = "";
		String displayNameText = "";
		String enchantmentText = "";

		if(identified && prefixEnchantment != null && prefixEnchantment.name != null && prefixEnchantment.name != "") {
			prefixEnchantmentText = this.getPrefixEnchantmentText();
		}

		if(itemCondition != null && itemCondition != ItemCondition.normal) {
			conditionText = getConditionText();
		}

		displayNameText = this.getDisplayName();

		if(identified && enchantment != null && enchantment.name != null && enchantment.name != "" && !name.contains("of")) {
			enchantmentText = this.getEnchantmentText();
		}

		t_name = MessageFormat.format(StringManager.get("entities.Item.itemNameText"), prefixEnchantmentText, conditionText, displayNameText, enchantmentText).replaceAll("\\s+", " ").trim();

		return t_name;
	}

	public String GetItemText() {
		return "";
	}

	private static Color RareItemColor = new Color(0.5f, 1f, 0.4f, 1f);
	private static Color MagicItemColor = new Color(0.1f, 0.8f, 1f, 1f);
	private static Color UniqueItemColor = new Color(0.9f, 0.7f, 0.1f, 1f);
	public Color GetTextColor() {
        if(unique) return UniqueItemColor;

		if(enchantment != null && prefixEnchantment != null) {
			return RareItemColor;
		}

		if(enchantment != null || prefixEnchantment != null) {
			return MagicItemColor;
		}

		return Color.WHITE;
	}

	public String GetInfoText() {
		String infoText = GetItemText();

		if(!identified && (prefixEnchantment != null || enchantment != null)) {
			return infoText + "\n" + StringManager.get("entities.Item.unidentifiedIndicatorText");
		}

		int moveMod = 0;
		int hpMod = 0;
		int agilityMod = 0;
		int armorMod = 0;
		int magicMod = 0;
		int attackMod = 0;
		float knockbackMod = 0;
		float attackSpeedMod = 0;
		float magicResistMod = 0;

		for(ItemModification m : getEnchantments()) {
			moveMod += m.getMoveSpeedMod(this);
			hpMod += m.getHpMod(this);
			agilityMod += m.getAgilityMod(this);
			armorMod += m.getArmorMod(this);
			magicMod += m.getMagicMod(this);
			attackMod += m.getAttackMod(this);
			knockbackMod += m.getKnockbackMod(this);
			attackSpeedMod += m.getAttackSpeedMod(this);
			magicResistMod += m.getMagicResistMod(this);
		}

		// add status texts
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoSpeedText"), moveMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoHealthText"), hpMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoAgilityText"), agilityMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoDefenseText"), armorMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoMagicText"), magicMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoAttackText"), attackMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoKnockbackText"), knockbackMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoAttackSpeedText"), attackSpeedMod);
		infoText += GetModificationInfoTextLine(infoText,StringManager.get("entities.Item.modificationInfoMagicResistText"), magicResistMod);

		return infoText;
	}

	private String GetModificationInfoTextLine(String textSoFar, String modName, int modAmount) {
		if(modAmount == 0) return "";
		String newLineOrNone = textSoFar.equals("") ? "" : "\n";
		return newLineOrNone + modName + ": " + (modAmount > 0 ? "+" : "") + modAmount;
	}

	private String GetModificationInfoTextLine(String textSoFar, String modName, float modAmountPercent) {
		if(modAmountPercent == 0) return "";
		String newLineOrNone = textSoFar.equals("") ? "" : "\n";
		String amount = String.format("%.3g%n", modAmountPercent * 10f).trim();

		// Why is number formatting in Java so hard?
		if(amount.endsWith(".0")) {
			amount = amount.replace(".0", "");
		}
		else if(amount.endsWith(".00")) {
			amount = amount.replace(".00", "");
		}

		return newLineOrNone + modName + ": " + (modAmountPercent > 0 ? "+" : "") + amount;
	}

	public void updateDrawable() {
		updateDrawableInternal(false);
	}

	public void updateHeldDrawable() {
		updateDrawableInternal(true);
	}

	protected void updateDrawableInternal(boolean held) {
		String meshToUse = getMeshToUse(held);
		if(meshToUse != null) {
			// Check if we need to create/update our mesh drawable
			if (!meshToUse.equals(lastMeshFile) || (textureFile != null && !textureFile.equals(lastTextureFile))) {
				String pickedMeshFile = meshToUse;
				if (meshToUse.contains(",")) {
					String[] files = meshToUse.split(",");
					pickedMeshFile = files[Game.rand.nextInt(files.length)];
				}

				String pickedTextureFile = textureFile;
				if (textureFile.contains(",")) {
					String[] files = textureFile.split(",");
					pickedTextureFile = files[Game.rand.nextInt(files.length)];
				}

				drawable = new DrawableMesh(pickedMeshFile, pickedTextureFile);
				lastMeshFile = meshToUse;
				lastTextureFile = textureFile;
			}
		} else {
			// Make sure we stop using the mesh version when done
			if(drawable instanceof DrawableMesh)
				drawable = null;
		}

		if(drawable != null) {
			drawable.update(this);
		}
		else if(artType != ArtType.hidden) {
			drawable = new DrawableSprite(tex, artType);
			drawable.update(this);
		}
	}

	public Integer getHeldTex() {
		if(heldTex != null) return heldTex;
		return tex;
	}

	public String GetEquipLoc() {
		return equipLoc;
	}

	public Integer getInventoryTex() {
		if (inventoryTex != null) return inventoryTex;
		return tex;
	}

	@Override
	public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator) {
		super.hit(projx, projy, damage, knockback, damageType, instigator);
		float force = Math.min(knockback, 0.7f);
		this.applyPhysicsImpulse(new Vector3(projx * force, projy * force, 0));

		damageItem(damage, damageType);

		if(isActive && (instigator != null && !(instigator instanceof Fire))) {
			Audio.playPositionedSound(dropSound, new Vector3(x,y,z), Math.max(0.125f, knockback * 4f), 1f, 10f);
		}
	}

	protected void damageItem(int damage, DamageType damageType) {
		// Damage items when hit
		if(itemType == ItemType.junk && damage > 0) {
			int newCondition = itemCondition.ordinal() - Math.max(1, damage / 8);
			if(newCondition >= 0)
				itemCondition = ItemCondition.values()[newCondition];
			else
				breakItem();
		}
	}

	private void breakItem() {
		isActive = false;

		// make dust puffs
		for(int i = 0; i < 5; i++) {
			Particle p = CachePools.getParticle(x - xa, y - ya, z - za - 0.24f, 0, 0, 0, Game.rand.nextInt(3), Color.WHITE, false);

			// Randomize location a tad
			float spawnOffset = 0.35f;
			p.x += (spawnOffset * Game.rand.nextFloat()) - spawnOffset * 0.5f;
			p.y += (spawnOffset * Game.rand.nextFloat()) - spawnOffset * 0.5f;
			p.z += (spawnOffset * Game.rand.nextFloat()) - spawnOffset * 0.5f;

			p.checkCollision = false;
			p.floating = true;
			p.lifetime = (int) (50 * Game.rand.nextFloat()) + 120;
			p.shader = "dust";
			p.spriteAtlas = "dust_puffs";
			p.startScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
			p.endScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
			p.endColor = new Color(1f, 1f, 1f, 0f);
			p.scale = 0.5f;

			p.xa = xa + (0.00125f * Game.rand.nextFloat());
			p.ya = ya + (0.00125f * Game.rand.nextFloat());
			p.za = za + (0.00125f * Game.rand.nextFloat()) + 0.0025f;

			p.maxVelocity = 0.0025f;

			Game.GetLevel().SpawnNonCollidingEntity(p);
		}

		// make small particles
		Random r = Game.rand;
		for(int ii = 0; ii < r.nextInt(9) + 14; ii++)
		{
			Particle p = CachePools.getParticle(x, y, z + 0.1f, xa * 0.2f + r.nextFloat() * 0.01f - 0.005f, ya * 0.2f + r.nextFloat() * 0.01f - 0.005f, za * 0.2f + r.nextFloat() * 0.03f - 0.015f, 420 + r.nextInt(500), 1f, 0f, 0, Colors.MUNDANE, false);
			p.movementRotateAmount = 10f;
			Game.GetLevel().SpawnNonCollidingEntity(p);
		}

		Audio.playPositionedSound("break/break_pottery_01.mp3,break/break_pottery_02.mp3,break/break_pottery_03.mp3", new Vector3(x,y,z), 0.8f, 1f, 10f);
	}

	@Override
	public void hitWorld(float xSpeed, float ySpeed, float zSpeed) {
		super.hitWorld(xSpeed, ySpeed, zSpeed);
		if(xSpeed != 0 || ySpeed != 0) {
			ignorePlayerCollision = false;

			Vector3 damageVector = new Vector3(xSpeed, ySpeed, zSpeed);
			damageItem((int)(damageVector.len() * 30f), DamageType.PHYSICAL);
		}
	}

	public String getConditionText() {
		String condition = itemConditionText[itemCondition.ordinal()];
		String form = StringManager.form(this.name);

		return StringManager.get("entities.Item.itemCondition" + condition, form);
	}

	public String getEnchantmentText() {
		String form = StringManager.form(this.name);
		return StringManager.get(this.enchantment.name, form);
	}

	public String getPrefixEnchantmentText() {
		String form = StringManager.form(this.name);
		return StringManager.get(this.prefixEnchantment.name, form);
	}

	public TextureRegion getInventoryTextureRegion() {
		TextureAtlas atlas = getTextureAtlas();
		return atlas.getSprite(getInventoryTex());
	}

	public TextureRegion getHeldInventoryTextureRegion(int offset) {
		TextureAtlas atlas = getTextureAtlas();
		return atlas.getSprite(getHeldTex() + offset);
	}

	public TextureAtlas getTextureAtlas() {
		TextureAtlas atlas = TextureAtlas.cachedAtlases.get(spriteAtlas);
		if(atlas == null) atlas = TextureAtlas.cachedAtlases.get(artType.toString());
		return atlas;
	}

	// Gather and return all enchantments
	protected transient Array<ItemModification> t_enchantments = new Array<ItemModification>();
	public Array<ItemModification> getEnchantments() {
		t_enchantments.clear();
		if(!identified) return t_enchantments;
		if(prefixEnchantment != null) t_enchantments.add(prefixEnchantment);
		if(enchantment != null) t_enchantments.add(enchantment);
		return t_enchantments;
	}

	// Gather and return all enchantments
	public Array<ItemModification> getEnchantmentsEvenUnidentified() {
		t_enchantments.clear();
		if(prefixEnchantment != null) t_enchantments.add(prefixEnchantment);
		if(enchantment != null) t_enchantments.add(enchantment);
		return t_enchantments;
	}

	// override this to take action when charging has started
	public void onChargeStart() { }

	// override this to take action when picked up
	public void onPickup() { }

	public boolean shouldUseMesh(boolean held) {
		String mesh = getMeshToUse(held);
		return mesh != null;
	}

	private String getMeshToUse(boolean held) {
		if(held) {
			if(viewMeshFile != null && !viewMeshFile.isEmpty())
				return viewMeshFile;
		}

		if(meshFile != null && !meshFile.isEmpty())
			return meshFile;

		return null;
	}
}
