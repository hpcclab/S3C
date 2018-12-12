import java.io.File;
import java.util.Scanner;

public class SemanticSearchEdge {
    /**
     * Application: - Get user command + Search: -s + Upload: -u + Remove: -r +
     * Decrypt: -d - Upon user command, ask for additional information
     * 
     * @param args
     */
    private static String option;
    private static String query;
    private static boolean hasArgs = false;
    private static String opt;

    public static void main(String[] args) throws Exception{

        Config.loadProperties();

        System.out.println("Welcome to the Secured Semantic Search over Encrypted " + "Data Over the Cloud");

        Scanner scan = new Scanner(System.in);

        if (args.length > 1) {
            hasArgs = true;
            option = args[0];
            query = args[1];
            if (args.length > 2)
                opt = args[2];
        }

        if (!hasArgs) {
                System.out.println("\t+ Search: -s" + "\n\t+ Upload: -u" + "\n\t+ Remove: -r " + "\n\t+ Decrypt: -d" + "\n\t+ Fetch: -f"+ "\n\t+ Key: -k"+ "\n\t+ "
                        + "Reset: -reset:");
                option = scan.nextLine();
        }


        switch (option) {
        /*
         * Searching part - Ask for query - Search option: * 0: query
         * combination only * 1: query combination + wikipedia + synonym * 2:
         * query combination + synonym * 3: query combination + wikipedia
         */
        case "-s":
                if (!hasArgs) {
                        System.out.println("Search for:");
                        query = scan.nextLine();
                        System.out.println("Option:");
                        opt = scan.nextLine();
                }
                Searcher_temp searcher;
                switch (opt) {
                case "0":
                        searcher = new SearcherKWO();
                        searcher.search(query);
                        break;
                case "1":
                        searcher = new SearcherWKSN();
                        searcher.search(query);
                        break;
                case "2":
                        searcher = new SearcherSN();
                        searcher.search(query);
                        break;
                case "3":
                        searcher = new SearcherWK();
                        searcher.search(query);
                        break;
                }
                break;

        /*
         * Uploading part - Ask for upload folder
         */
        case "-u":
                if (!hasArgs) {
                        System.out.println("Enter the upload folder:");
                        query = scan.nextLine();
                        System.out.println("Enter desired destination (Google Drive, Dropbox, or HPCC cloud):");
                        switch(scan.nextLine()){
                            case ("Google Drive"):
                                opt = "-gd";
                                break;
                            case ("Dropbox"):
                                opt = "-db";
                                break;
                            case ("HPCC cloud"):
                                opt = "-hc";
                                break;
                            default:
                                System.out.println("Not a valid destination.");
                                System.exit(0);

                        }
                }
                File upload = new File(query);

                if (!upload.exists()) {
                        System.out.println("Folder does not exist");
                        System.exit(0);
                } else {
                        Uploader uploader = new Uploader(query, opt);
                        uploader.upload();
                }
                break;
        
        // Makes key files of all the files in the designated folder.
        // Separate from uploads because of complications with the PHP
        case "-k":
                if (!hasArgs) {
                    System.out.println("Enter the upload folder:");
                    query = scan.nextLine();
                }
                File uploadKey = new File(query);

                if (!uploadKey.exists()) {
                    System.out.println("Folder does not exist");
                    System.exit(0);
                } else {
                    Uploader uploader = new Uploader(query);
                    uploader.keyUpload();
                }
                break;

        /*
         * Removing part - Ask for file to remove
         */
        case "-r":
                if (!hasArgs) {
                        System.out.println("Enter the file to remove:");
                        query = scan.nextLine();
                }
                Remover remover = new Remover();
                remover.remove(query);
                break;

        /*
         * Decrypting part - Ask for file to decrypt
         */
        case "-d":
                if (!hasArgs) {
                        System.out.println("Ener the file to be decrypted:");
                        query = scan.nextLine();
                }
                Decrypter decrypter = new Decrypter();
                decrypter.decrypt(query);
                break;

        /*
        Fetch - get a file from one of the repositories. Would be more useful right after a search to give people the option, but the separation makes it more modular.
        */
        case "-f":
            if (!hasArgs){
                System.out.println("Enter a repository to fetch from (Google Drive, Dropbox, or HPCC cloud):");
                    switch(scan.nextLine()){
                        case ("Google Drive"):
                            query = "-gd";
                            break;
                        case ("Dropbox"):
                            query = "-db";
                            break;
                        case ("HPCC cloud"):
                            query = "-hc";
                            break;
                        default:
                            System.out.println("Not a valid destination.");
                            System.exit(0);

                    }
                System.out.println("Enter what file from that repository you would like:");
                opt = scan.nextLine();
            }

            switch (query){
                case "-gd" :
                    try {
                        GoogleDriveAPI gdrive = new GoogleDriveAPI();
                        gdrive.fetch(opt);
                    } catch (Exception e) {}
                    break;
                case "-db" :
                    try {
                        DropboxAPI dbdrive = new DropboxAPI();
                        dbdrive.fetch(opt);
                    } catch (Exception e) {}
                    break;
                default:
                    System.out.println("Currently unavailable.");
            }
            break;
        
        // Completely resets the Google Drive service account (info in GoogleDriveAPI)
        case "-reset":
            GoogleDriveAPI dump = new GoogleDriveAPI();
            dump.reset();
            break;
        }
        System.exit(0);
    }
}
