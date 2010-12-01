package adfgvx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(Main.class);

    public Main(String cipherText, String pat, String tet) {
	try {
	    Pattern pattern = new Pattern(pat);
	    Tetagram tetagram = new Tetagram(tet);
	    Analysis analysis = new Analysis(pattern, tetagram);
	    
	    String largeText = readCipher(cipherText);
	    
	    //analysis.doAnalysis(largeText);
	    analysis.doHillclimbTestRun(largeText);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	
    }

    public String readCipher(String filename) {
	String file;
	try {
	    file = new Scanner(new File(filename)).useDelimiter("\\Z").next();
	    file = file.replaceAll("[^A-Z]", "");
	    return file;
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}

	return null;
    }

    public static void main(String[] args) {
	BasicConfigurator.configure();
	LOG.setLevel(Level.INFO);
	LOG.info("Started");

	Options options = new Options();
	options.addOption("v", false, "Be verbose");
	options.addOption("c", true, "Cipher text");
	options.addOption("p", true, "Patterns");
	options.addOption("t", true, "Tetagrams");
	options.addOption("help", false, "Display help message");

	CommandLineParser parser = new GnuParser();
	try {
	    CommandLine cmd = parser.parse(options, args);

	    if (cmd.hasOption("help")) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("adfgvSolver", options);

		return;
	    }

	    if (cmd.hasOption("c") && cmd.hasOption("p") && cmd.hasOption("t")) {
		new Main(cmd.getOptionValue("c"), cmd.getOptionValue("p"),
			cmd.getOptionValue("t"));
	    } else {
		LOG.error("Please check input.");
	    }

	} catch (ParseException e) {
	    LOG.info("Could not parse arguments. Pass -help for help."
		    + e.getMessage());
	}

    }
}
