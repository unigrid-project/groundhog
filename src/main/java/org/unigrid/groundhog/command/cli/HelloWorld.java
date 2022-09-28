package org.unigrid.groundhog.command.cli;

import picocli.CommandLine;

@CommandLine.Command(
  name = "hello"
)
public class HelloWorld implements Runnable {
    @Override
    public void run() {
        System.out.println("fuck off you fucking fuck");
    }
}