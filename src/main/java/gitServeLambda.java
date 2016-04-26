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

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context; 

import com.google.common.io.Files;
import com.restfb.json.JsonObject;

public class GitServeLambda implements RequestHandler<LambdaHttpServletRequest, LambdaHttpServletResponse> {

    
    @Override
    public LambdaHttpServletResponse handleRequest(LambdaHttpServletRequest request, Context context)
    {
        LambdaHttpServletResponse response = new LambdaHttpServletResponse();
        
        //get uri for file
        String urlPath = request.getRequestUri();
        // build file

        //serve file
        OutputStream out = response.getOutputStream();
        
        out.write(builtFile.getBytes());
        out.close();
        return response;
    }


    public static void main(String[] args) throws InvalidRemoteException, 
                          TransportException, GitAPIException, IOException  {

        

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

            
        }
        finally
        {
            if(git != null) git.close();
            FileUtils.deleteDirectory(sourceRepo);
        }


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
