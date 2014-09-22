package legacycode.infra;

import legacycode.infra.DB;


/**
 * <b>Legacy tool</b> to setup production like environment on the local workstation for <b>manual</b> "testing" after deploy.
 *
 * Not to be used in unit tests.
 */
public class SetupDB {

    public static void main(String[] args) {

        DB.start();
        DB.runScript("createDB.sql");
        DB.runScript("populateDB.sql");

    }
}
