import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.FetchResult;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import com.google.common.io.Files;
import com.restfb.json.JsonObject;

public class GitServeLambda implements RequestHandler<LambdaHttpServletRequest, LambdaHttpServletResponse> {

    
    @Override
    public LambdaHttpServletResponse handleRequest(LambdaHttpServletRequest request, Context context)
    {
        LambdaHttpServletResponse response = new LambdaHttpServletResponse();
        
        //get uri for file

        // build file

        //serve file
        OutputStream out = response.getOutputStream();
        
        out.write(builtFile.getBytes());
        out.close();
        return response;
    }


    public static void main(String[] args) throws InvalidRemoteException, 
                          TransportException, GitAPIException, IOException,
                           {

        

        final File sourceRepo = Files.createTempDir();
        Git git = null;
        try 
        {
            System.out.println("Cloning repository");
            git = Git.cloneRepository()
                    .setURI("https://github.com/gregorydickson/angular-dashboard.git")
                    .setDirectory(sourceRepo)
                    .call();

            git.fetch().setRemote("origin").setCheckFetchedObjects(true).call();
            git.pull().setRemote("origin").setRebase(true).setRemoteBranchName("master").call();

            System.out.println(sourceRepo);
            npmInstall(sourceRepo);
            gruntBuild(sourceRepo);
            System.out.println(sourceRepo);

            String version = "0.0."+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            packageAndDeploy(new File(new File(new File(sourceRepo, "build"), "packages"), "react.tgz"), version);
            packageAndDeploy(new File(new File(new File(sourceRepo, "build"), "packages"), "react-dom.tgz"), version);
        }
        finally
        {
            if(git != null) git.close();
            FileUtils.deleteDirectory(sourceRepo);
        }


    }

    public static void packageAndDeploy(File tarball, String version) throws IOException
    {
        File temp = Files.createTempDir();
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(tarball, temp);
        System.out.println(temp);
        File packageJson = new File(new File(temp, "package"), "package.json");
        JsonObject json = new JsonObject(FileUtils.readFileToString(packageJson));
        json.put("name", json.get("name")+"-future");
        json.put("version", version);
        json.put("description", "A future/experimental build of React with features and functionality being added/considered for a future version of React.  This build follows master, which means it is up-to-date and passes unit tests but is not as stable as a stable release."); //   Used for integration testing, research, and development.

        // Currently not setting the dependency on react-future (leaving it depend on React) since react-future isn't strictly necessary and we want to maintain compatibility
/*      if(!"react-future".equals(json.get("name")))
        {
            JsonObject dependencies = new JsonObject();
            dependencies.put("react-future", "^0.0."+version);
            json.put("dependencies", dependencies);
        }
*/      if("react-future".equals(json.get("name")))
        {
            File readmeFile = new File(new File(temp, "package"), "README.md");
            String readme = FileUtils.readFileToString(readmeFile);
            readme = readme.replace("npm install react", "npm install react-future");
            FileUtils.writeStringToFile(readmeFile, readme);
        }
        if("react-dom-future".equals(json.get("name")))
        {
            File readmeFile = new File(new File(temp, "package"), "README.md");
            String readme = FileUtils.readFileToString(readmeFile);
            //readme = readme.replace("require('react')", "require('react-future')");
            readme = readme.replace("react-dom", "react-dom-future");
            FileUtils.writeStringToFile(readmeFile, readme);

            json.getJsonObject("peerDependencies").remove("react");
            json.getJsonObject("peerDependencies").put("react-future", version);
        }
        FileUtils.writeStringToFile(packageJson, json.toString());
        npmPublish(new File(temp, "package"));
        FileUtils.deleteDirectory(temp);
    }

    public static void npmPublish(File directory) throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder("/Users/jsproch/.nvm/v4.1.1/bin/npm", "publish");
        pb.directory(directory);
        pb.environment().put("PATH", "/Users/jsproch/.nvm/v4.1.1/bin/:/opt/facebook/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/local/munki");
        pb.environment().put("NODE_PATH", "/Users/jsproch/.nvm/v4.1.1/lib/node_modules");
        pb.redirectErrorStream(true);

        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while((line = reader.readLine()) != null)
        {
            System.out.println(line);
        }
    }

    public static void npmInstall(File directory) throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder("/Users/jsproch/.nvm/v4.1.1/bin/npm", "install");
        pb.directory(directory);
        pb.environment().put("PATH", "/Users/jsproch/.nvm/v4.1.1/bin/:/opt/facebook/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/local/munki");
        pb.environment().put("NODE_PATH", "/Users/jsproch/.nvm/v4.1.1/lib/node_modules");
        pb.redirectErrorStream(true);

        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while((line = reader.readLine()) != null)
        {
            System.out.println(line);
        }
    }

    public static void gruntBuild(File directory) throws IOException
    {
        ProcessBuilder pb = new ProcessBuilder("/Users/jsproch/.nvm/v4.1.1/bin/grunt", "build");
        pb.directory(directory);
        pb.environment().put("PATH", "/Users/jsproch/.nvm/v4.1.1/bin/:/opt/facebook/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/local/munki");
        pb.environment().put("NODE_PATH", "/Users/jsproch/.nvm/v4.1.1/lib/node_modules");
        pb.redirectErrorStream(true);

        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        boolean success = false;

        String line;
        while((line = reader.readLine()) != null)
        {
            System.out.println(line);
            if(line.contains("Done, without errors.")) success = true;
        }

        if(!success) throw new Error("Grunt build appears to have failed!");
    }
}
