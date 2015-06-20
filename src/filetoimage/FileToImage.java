package filetoimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import static java.lang.Math.*;
import java.nio.file.*; 
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Reads a specified file and converts its byte data into colour data, using it
 * to produce a square (or near-square) image.
 * 
 * @author Jonathan Humphreys
 */
public class FileToImage
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        String filePath;
        
        if(args.length > 0)
        {
            filePath = args[args.length - 1];
        }
        else
        {
            Scanner kb = new Scanner(System.in);
            System.out.print("Enter a file name: ");
            filePath = kb.nextLine();
        }
        
        byte[] fileData = getFileBytes(filePath);
        
        ArrayList colourList = buildColourList(fileData);
        BufferedImage finalImage = createImage(colourList);
        
        if(saveImage(finalImage))
        {
            System.out.println("All done!");
        }
        else
        {
            System.out.println("Unable to create image!");
        }
    }
    
    
    /**
     * Reads a file and outputs its byte data to an array.
     * 
     * @param filePath The location of the file to be read.
     * @return An array containing the individual bytes of that file.
     */
    private static byte[] getFileBytes(String filePath)
    {
        System.out.println("Getting bytes from file...");
        Path inputPath = Paths.get(filePath);
        byte[] fileData = {};
        
        try
        {
            fileData = Files.readAllBytes(inputPath);
        } catch (IOException ex)
        {
            Logger.getLogger(FileToImage.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return fileData;
    }
    
    /**
     * Constructs an ArrayList of colours. Each colour is created from three
     * bytes read from the input file, each byte forming the red, green and blue
     * values of that colour. If there are no more bytes, the remainder of that
     * colour's values remain 0.
     * 
     * @param fileData The bytes read from the input file.
     * @return The list of colours constructed from those bytes.
     */
    private static ArrayList buildColourList(byte[] fileData)
    {
        System.out.println("Assembling pixel colour list...");
        
        int counter = 0;
        byte[] colourBytes = {0, 0, 0};
        ArrayList pixelColours = new ArrayList();
        
        for(int b = 0; b < fileData.length; b++)
        {
            colourBytes[counter] = fileData[b];
            counter++;
            
            if(counter > 2)
            {
                counter = 0;
                pixelColours.add(new Color(getUnsignedInt(colourBytes[0]),
                                           getUnsignedInt(colourBytes[1]),
                                           getUnsignedInt(colourBytes[2])));
                
                for(int i = 0; i < colourBytes.length; i++)
                {
                    colourBytes[i] = 0;
                }
            }
            
            updateProgress(b, b - 1, fileData.length - 1);
        }
        
        if(counter > 0)
        {
            pixelColours.add(new Color(getUnsignedInt(colourBytes[0]),
                                       getUnsignedInt(colourBytes[1]),
                                       getUnsignedInt(colourBytes[2])));
        }
        
        System.out.println();
        return pixelColours;
    }
    
    
    /**
     * Creates an image, as square as possible, based on the size of the
     * ArrayList of colour data provided.
     * 
     * @param pixelColours The list of colours for each pixel.
     * @return A buffered image, square or near-square in ratio, with the
     *         provided pixel colour data drawn to it.
     */
    private static BufferedImage createImage(ArrayList pixelColours)
    {
        System.out.println("Creating image...");
        int maxWidth = (int) round(sqrt(pixelColours.size()));
        int maxHeight = (int) ceil((double) pixelColours.size() / maxWidth);
        
        BufferedImage finalImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        
        return drawImage(pixelColours, finalImage);
    }
    
    /**
     * Takes an array list of Color objects and draws pixels of each colour to
     * their respective position on a buffered image.
     * 
     * @param pixelColours The sequence of colours to draw to the image.
     * @param finalImage The image upon which to draw.
     * @return A completed buffered image.
     */
    private static BufferedImage drawImage(ArrayList pixelColours, BufferedImage finalImage)
    {        
        System.out.println("Drawing pixel data to image...");
        
        int progress = 0;
        
        for(int y = 0; y < finalImage.getHeight(); y++)
        {
            for (int x = 0; x < finalImage.getWidth(); x++)
            {
                if(progress < pixelColours.size())
                {
                    Color current = (Color) pixelColours.get(progress);
                    finalImage.setRGB(x, y, current.getRGB());
                }
                else
                {
                    finalImage.setRGB(x, y, 0);
                }

                updateProgress(progress, progress - 1, pixelColours.size() - 1);
                progress++;
            }
        }
        System.out.println();
        
        return finalImage;
    }
    
    /**
     * Writes a progress bar to the command line, keeping it on the same line.
     * Good for ensuring that the damn program is still running.
     * 
     * @param newProgress The new value representing the program's progress
     *                    toward its current goal.
     * @param oldProgress The previous value representing the program's progress
     *                    since this method was last called.
     * @param target      The target value.
     */
    private static void updateProgress(int newProgress, int oldProgress, int target)
    {
        int newProgressPercent = (int) (((double) newProgress / (double) target) * 100);
        int oldProgressPercent = (int) (((double) oldProgress / (double) target) * 100);
        
        int newProgressTen = (int) ((double) newProgressPercent / 10 % 10);
        int oldProgressTen = (int) ((double) oldProgressPercent / 10 % 10);
        
        if(newProgressTen != oldProgressTen)
        {
            String progressBar = "[";

            for(int i = 0; i < 10; i++)
            {
                if(i * 10 < newProgressPercent)
                {
                    progressBar += "#";
                }
                else
                {
                    progressBar += "-";
                }
            }

            progressBar += "]" + String.format(" %3d%%\r", newProgressPercent);

            System.out.print(progressBar);
        }
    }
    
    /**
     * Writes the contents of the buffered image to file.
     * 
     * @param finalImage The image to write to file.
     * @return A boolean to declare whether the act was successful.
     */
    private static boolean saveImage(BufferedImage finalImage)
    {
        System.out.println("Saving image...");
        boolean fileSaved = false;
        
        try
        {
            File outputFile = new File("saved.png");
            ImageIO.write(finalImage, "png", outputFile);
            fileSaved = true;
        }
        catch(IOException e)
        {
            System.out.println("Error: " + e);
        }
        
        return fileSaved;
    }
    
    /**
     * Checks whether a byte is a negative and corrects it to a positive. By
     * adding 128 twice.
     * Because fucking Java can't do unsigned for shit.
     * 
     * @param inputByte The byte to evaluate.
     * @return An integer representing the byte's unsigned value.
     */
    private static int getUnsignedInt(byte inputByte)
    {
        if(inputByte < 0)
        {
            return (int) inputByte + (128 * 2);
        }
        
        return inputByte;
    }
}
