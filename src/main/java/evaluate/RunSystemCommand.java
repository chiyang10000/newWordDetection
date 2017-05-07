package evaluate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by don on 07/05/2017.
 */
public class RunSystemCommand {
	static String shell = "bash", option = "-c";
	static private Logger logger = LoggerFactory.getLogger(RunSystemCommand.class);

	public static void run(String cmd) {
		try {
			logger.debug("Running command: [{}]", cmd);
			String[] cmds = new String[]{shell, option, cmd};
			Process pro = Runtime.getRuntime().exec(cmds);
			InputStream in = pro.getInputStream();
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = read.readLine()) != null) {
				System.err.println(line);
			}
			in.close();
			pro.waitFor();
			in = pro.getErrorStream();
			read = new BufferedReader(new InputStreamReader(in));
			while ((line = read.readLine()) != null) {
				System.err.println(line);
			}
			in.close();
		} catch (Exception e) {
			logger.debug("Run command err! : [{}] ", cmd);
			e.printStackTrace();
		}
	}
}
