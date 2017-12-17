package net.schoperation.schopcraft.season;

import java.lang.reflect.Field;
import java.util.Iterator;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.schoperation.schopcraft.SchopCraft;

public class BiomeTemp {
	
	/*
	 * This stores the original biome temperatures, modifying the base temps if necessary.
	 * It also deals with changing the temperature of each biome according to the season, and where we are in the season.
	 */
	
	// The array of temperatures. Plz don't touch it outside of this class. If outside, you can only look.
	public static float[] temperatures;
	
	// This method goes through every biome and grabs the original temperature. Called at the beginning.
	public static void storeOriginalTemperatures() {
		
		// Number of biomes
		int biomeNumber = ForgeRegistries.BIOMES.getValues().size();
		
		// Allocate enough space for array
		temperatures = new float[biomeNumber];
		
		// Iterator
		Iterator<Biome> biomeList = ForgeRegistries.BIOMES.getValues().iterator();
		int counter = 0;
		
		// Iterate kek
		while (biomeList.hasNext()) {
			
			// The biome
			Biome choosenBiome = biomeList.next();
			
			// Grab the temperature and store it and go
			float biomeTemp = choosenBiome.getDefaultTemperature();
			temperatures[counter] = biomeTemp;
			counter++;
		}
		
		SchopCraft.logger.info("Stored " + (counter + 1) + " biome temperatures.");
	}
	
	// Want some original temp for a specific biome? Use this thingy-a-method here!
	public static float getOriginalTemperature(Biome biome) {
		
		// Iterator
		Iterator<Biome> biomeList = ForgeRegistries.BIOMES.getValues().iterator();
		int counter = 0;
		
		// Iterate kek
		while (biomeList.hasNext()) {
			
			// The biome
			Biome choosenBiome = biomeList.next();
			
			// Is this the right one?
			if (biome.equals(choosenBiome)) {
				
				return temperatures[counter];
			}
			
			else {
				
				counter++;
			}
		}
		
		return counter;
	}
	
	// This actually changes the biome temperatures every morning
	// Using reflection! Yeah it's very hacky, hackish, whatever you wanna call it but it works.
	public static void changeBiomeTemperatures(Season season, int daysIntoSeason) {
		
		// Get all of the biomes and iterate through them
		Iterator<Biome> biomeList = ForgeRegistries.BIOMES.getValues().iterator();
		int counter = 0;
		
		// Iterate kek
		while (biomeList.hasNext()) {
			
			// The biome
			Biome choosenBiome = biomeList.next();
			
			// Its original temperature
			float origTemp = getOriginalTemperature(choosenBiome);
			
			// Half of the season.
			int halfSeason = season.getLength(season) / 2;
			
			// The temperature changes everyday, so it's more gradual.
			// Every half of each season has a specified temperature change that should be COMPLETELY added to the ORIGINAL temperature by the end of said half.
			// We can get the total temperature change through the Season class methods.
			float tempDiff = season.getTemperatureDifference(daysIntoSeason);
			
			// Depending on how far we are into the season, we'll only take a portion of the temp difference and add it to the original biome temperatures.
			// Day: 1 2 3 4 5 6 7 8 9 10 11 12 13 14  1
			//      --------------********************-
			// - = first difference, * = second difference
			
			// Our base temp is NOT the original. We'll start out by applying the previous season's difference immediately. (Second half of previous)
			// If we're in a middle of a season, don't use the previous season, use the same one. (First half of current)
			float prevTempDiff;
			
			if (daysIntoSeason > halfSeason) {
				
				prevTempDiff = season.getTemperatureDifference(0);
			}
			
			else {
				
				Season prevSeason = season.prevSeason(season);
				prevTempDiff = prevSeason.getTemperatureDifference(halfSeason + 1);
			}
			
			// Instantly apply that one
			// never mind
			
			// Now apply the CURRENT temp difference to the original temp. That's the temp we have to get to.
			float targetTemp = origTemp + tempDiff;
			
			// The difference between the target temp and current temp
			float actualDiff = targetTemp - (origTemp + prevTempDiff);
			
			// Divide this difference by the amount of days in half a season.
			float diffPerDay = actualDiff / halfSeason;
			
			// Now, we must edit daysIntoSeason so we can correctly calculate the difference to add onto the temperature.
			int halfDays = 0;
			if (daysIntoSeason > halfSeason) {
				
				halfDays = daysIntoSeason - halfSeason;
			}
			
			else {
				
				halfDays = daysIntoSeason;
			}
			
			// Create the REAL difference
			float newDiff = diffPerDay * halfDays;
			
			// Apply the differences. First the previous one from the previous half season. Then the new one we just calculated.
			try {
				Field f = Biome.class.getDeclaredField("temperature");
				f.setAccessible(true);
				f.set(choosenBiome, (origTemp + prevTempDiff + newDiff));
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			// Now do this for every fricking biome there is!
			// TODO add exceptions (ex. make jungles not become winter wonderlands in the winter)
		}
	}
}
