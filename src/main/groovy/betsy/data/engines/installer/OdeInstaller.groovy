package betsy.data.engines.installer

import static betsy.data.engines.installer.Constants.DOWNLOADS_DIR

class OdeInstaller {

    AntBuilder ant = new AntBuilder()

    String fileName = "apache-ode-war-1.3.5.zip"
    String downloadUrl = "https://lspi.wiai.uni-bamberg.de/svn/betsy/${fileName}"
    String serverDir = "server/ode"
    String odeName = "apache-ode-war-1.3.5"

    public void install() {
        TomcatInstaller tomcatInstaller = new TomcatInstaller(destinationDir: serverDir)
        tomcatInstaller.install()

        ant.get(dest: DOWNLOADS_DIR, skipexisting: true) {
            ant.url url: downloadUrl
        }

        ant.unzip src: "${DOWNLOADS_DIR}/${fileName}", dest: serverDir
        ant.unzip src: odeWar, dest: "${serverDir}/${tomcatInstaller.tomcatName}/webapps/ode"
        ant.copy file: "src/main/resources/ode/log4j.properties", todir: "${serverDir}/${tomcatInstaller.tomcatName}/webapps/ode/WEB-INF/classes", overwrite: true
    }

    String getOdeWar() {
        "${serverDir}/${odeName}/ode.war"
    }

}