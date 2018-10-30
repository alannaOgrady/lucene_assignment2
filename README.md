mvn package
java -cp target/assignment-2-1.0-SNAPSHOT.jar com.mycompany.app.App

When getting class not found: mvn clean dependency:copy-dependencies package

to check if executes: mvn exec:java -D exec.mainClass=com.mycompany.app.App