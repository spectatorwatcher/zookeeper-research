package org.apache.zookeeper.cli;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

public class TreeCommand extends CliCommand {

    private static Options options = new Options();
    private String args[];
    
    public TreeCommand() {
        super("tree", "path [watch]");
    }
    
    @Override
    public CliCommand parse(String[] cmdArgs) throws CliParseException {
        Parser parser = new PosixParser();
        CommandLine cl;
        try {
            cl = parser.parse(options, cmdArgs);
        } catch (ParseException ex) {
            throw new CliParseException(ex);
        }
        args = cl.getArgs();
        if (args.length < 2) {
            throw new CliParseException(getUsageStr());
        }
        
        return this;
    }

    @Override
    public boolean exec() throws CliException {
        err.println("'tree' was a new command. "
                  + "Please use as 'tree path depth'.");
        String path = args[1];
        boolean watch = args.length > 2;
        Stat stat = new Stat();
        int depth = args.length == 2 ? Integer.MAX_VALUE : Integer.valueOf(args[2]);
    	String pathtree;
		try {
			pathtree = listdir(path, watch, 0, depth);
		} catch (KeeperException|InterruptedException | IOException ex) {
            throw new CliWrapperException(ex);
        }
        out.println(pathtree);
        new StatPrinter(out).print(stat);
        return watch;
    }
    
    private String listdir(String path, boolean watch, int level, int depth) throws KeeperException, IOException, InterruptedException {
    	String pathtree = "\n";
    	for (int i = 0; i < level; i++) pathtree += "\t";
		pathtree += "|--" + path.substring(path.lastIndexOf("/"));
		List<String> children = zk.getChildren(path, watch);
		if (children.size() > 0) {
			level++;
			if (depth > level) {
				for (String sonpath : children) {
					pathtree +=	listdir(path + "/" + sonpath, watch, level, depth);
				}
			}
		}
		return pathtree;
	}
}