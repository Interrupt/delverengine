package com.interrupt.dungeoneer.gfx;

import com.interrupt.dungeoneer.Art;

public class Bitmap {
	public final int width;
	public final int height;
	public final int[] pixels;
	
	public Bitmap(int width, int height) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
	}
	
	public void draw(Bitmap bitmap, int xOffset, int yOffset)
	{
		for(int y = 0; y < bitmap.height; y++)
		{
			int yPix = y + yOffset;
			if(yPix < 0 || yPix >= height) continue;
			for (int x = 0; x < bitmap.width; x++)
			{
				int xPix = x + xOffset;
				if(xPix < 0 || xPix >= width) continue;
				
				int src = bitmap.pixels[x + y * bitmap.width];

				pixels[xPix + yPix * width] = src;
			}
		}
	}
	
	public void draw(Bitmap bitmap, int xOffset, int yOffset, int scale)
	{
		for(int y = 0; y < bitmap.height * scale; y++)
		{
			int yPix = y + yOffset;
			if(yPix < 0 || yPix >= height) continue;
			for (int x = 0; x < bitmap.width * scale; x++)
			{
				int xPix = x + xOffset;
				if(xPix < 0 || xPix >= width) continue;
				
				int pixel = bitmap.pixels[x / scale + y / scale * bitmap.width] & 0xffffff;
				if(pixel != 0xff00dc)
					pixels[xPix + yPix * width] = pixel;
			}
		}
	}
	
	public void drawText(String text, int xOffset, int yOffset, int color)
	{
		if(text == null) return;
		for(int i = 0; i < text.length(); i++)
		{
			if(text.charAt(i) == '\n') yOffset += 8;
			else drawChar(text.charAt(i), xOffset + i * 8, yOffset, color);
		}
	}
	
	public void drawChar(char toDraw, int xOffset, int yOffset, int color)
	{
		int charPos = Art.fontchars.indexOf(toDraw);
		if(charPos == -1) return;
		
		int drawY = (charPos / 16) * 8;
		int drawX = (charPos % 16) * 8;
		
		for(int x = 0; x < 8; x++)
		{
			for(int y = 0; y < 8; y++)
			{
				int fontpix = Art.font.pixels[(x + drawX) + (y + drawY) * Art.font.width];
				if(fontpix != 0xff00dc)
					pixels[(xOffset + x) + (yOffset + y) * width] = color;
			}
		}
	}
	
	public void clear(int clearcolor)
	{
		for(int i = 0; i < width * height; i++)
		{
			pixels[i] = clearcolor;
		}
	}
	
	public void mult(int color)
	{
		for(int i = 0; i < width * height; i++)
		{
			int r = (pixels[i] >> 16) & 0xff;
			int g = (pixels[i] >> 8) & 0xff;
			int b = pixels[i] & 0xff;
			
			int mr = (color >> 16) & 0xff;
			int mg = (color >> 8) & 0xff;
			int mb = color & 0xff;
			
			r += mr;
			g += mg;
			b += mb;
			
			if(r > 255) r = 255;
			if(g > 255) g = 255;
			if(b > 255) b = 255;
			
			pixels[i] = (r << 16) | (g << 8) | b;
		}
	}
	
	public Bitmap getSubimage(int x, int y, int subWidth, int subHeight) {
		Bitmap copy = new Bitmap(subWidth, subHeight);
		copy.draw(this, -x, -y);
		return copy;
	}

	public void rotateClockwise() {
		int[] copyPixels = pixels.clone();
		
		// rotate the array 90 degrees CW
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				pixels[x + y * width] = copyPixels[y + (width - x - 1) * width];
			}
		}
	}
	
	public void rotateCounterClockwise() {
		int[] copyPixels = pixels.clone();
		
		// rotate the array 90 degrees CCW
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				pixels[x + y * width] = copyPixels[(width - y - 1) + x * width];
			}
		}
	}
}
