package net.minecraft.server;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

@SideOnly(Side.SERVER)
public class ServerEula
{
    private static final Logger LOG = LogManager.getLogger();
    private final File eulaFile;
    private final boolean acceptedEULA;

    public ServerEula(File eulaFile)
    {
        this.eulaFile = eulaFile;
        this.acceptedEULA = loadEULAFile(eulaFile);
    }

    private boolean loadEULAFile(File inFile)
    {
        FileInputStream fileinputstream = null;
        boolean flag = false;

        try
        {
            Properties properties = new Properties();
            fileinputstream = new FileInputStream(inFile);
            properties.load(fileinputstream);
            flag = Boolean.parseBoolean(properties.getProperty("eula", "true"));
        }
        catch (Exception var8)
        {
            LOG.warn("Failed to load {}", (Object)inFile);
            this.createEULAFile();
        }
        finally
        {
            IOUtils.closeQuietly((InputStream)fileinputstream);
        }

        return flag;
    }

    public boolean hasAcceptedEULA()
    {
        return this.acceptedEULA;
    }

    public void createEULAFile()
    {
        FileOutputStream fileoutputstream = null;

        try
        {
            Properties properties = new Properties();
            fileoutputstream = new FileOutputStream(this.eulaFile);
            properties.setProperty("eula", "false");
            properties.store(fileoutputstream, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).");
        }
        catch (Exception exception)
        {
            LOG.warn("Failed to save {}", this.eulaFile, exception);
        }
        finally
        {
            IOUtils.closeQuietly((OutputStream)fileoutputstream);
        }
    }
}
