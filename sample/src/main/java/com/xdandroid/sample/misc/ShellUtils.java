package com.xdandroid.sample.misc;

import java.io.*;

interface ShellUtils {

    String COMMAND_SU = "su";
    String COMMAND_SH = "sh";
    String COMMAND_EXIT = "exit\n";
    String COMMAND_LINE_END = "\n";

    default CommandResult execCommand(String[] commands, boolean isRoot) {
        int result = -1;
        if (commands == null || commands.length == 0) return new CommandResult(result, null, null);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) continue;
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            result = process.waitFor();
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) successMsg.append(s);
            while ((s = errorResult.readLine()) != null) errorMsg.append(s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) os.close();
                if (successResult != null) successResult.close();
                if (errorResult != null) errorResult.close();
            } catch (IOException e) { e.printStackTrace(); }
            if (process != null) process.destroy();
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null : errorMsg.toString());
    }

    class CommandResult {

        int result;
        String successMsg;
        String errorMsg;

        CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
