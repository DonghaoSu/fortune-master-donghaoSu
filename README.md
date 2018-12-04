FORTUNE

# Running The Application

You need to use `curl` command to interact with your program.

You need to use `git` to clone this repository to your computer.
* https://git-scm.com/downloads
* `git clone https://github.com/borderxlab/fortune.git`
* go to the directory

You need to use MAVEN to compile java projects. https://maven.apache.org/

For MacOS, best is to use brew to install maven.
* https://brew.sh/
* http://brewformulas.org/Maven

For Linux, use `apt-get` to install `git` and `maven`.

For Windows, it may be tricky, if you don't already know how to use `git` and `maven`. See https://maven.apache.org/, which does not give you easiest way to get `maven`. Another option is to use `cygwin` (www.cygwin.com) on Windows to provide a Linux like environment. Windows 10 has a ubuntu subsystem (https://docs.microsoft.com/en-us/windows/wsl/install-win10) that can provide a Linux like environment. Yet another way is to run a Linux Virtual machine or Docker on Windows.

Try this to see MAVEN is working.

        mvn --version

To test the example application run the following commands.

* To create the example, package the application using [Apache Maven](https://maven.apache.org/) from the directory of the project.

        mvn clean package

* To run the server run.

        java -jar target/dropwizard-example-1.4.0-SNAPSHOT.jar server example.yml

* To hit the Hello World example from browser (hit refresh a few times).

	http://localhost:8080/hello-world

* Look at HelloWorldResource class source code comments to see how to interact with the API
        https://github.com/borderxlab/fortune/blob/master/src/main/java/com/example/helloworld/resources/HelloWorldResource.java

* Look at this line: (This is where the entry point of the jar is specified.)
        https://github.com/borderxlab/fortune/blob/master/pom.xml#L181


