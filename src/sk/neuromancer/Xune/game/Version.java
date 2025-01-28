package sk.neuromancer.Xune.game;

import picocli.CommandLine;

public class Version implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        String version = Version.class.getPackage().getImplementationVersion();
        if (version != null) {
            return new String[]{version};
        } else {
            return new String[0];
        }
    }
}
