package betsy.data.engines.bpelg


import betsy.data.BetsyProcess
import betsy.data.engines.LocalEngine;
import betsy.data.engines.Tomcat;
import betsy.data.engines.Util;

class BpelgEngine extends LocalEngine {

	@Override
	String getName() {
		"bpelg"
	}

	String getDeploymentDir() {
		"${tomcat.tomcatDir}/bpr"
	}

	@Override
	void storeLogs(BetsyProcess process) {
		ant.mkdir(dir: "${process.targetPath}/logs")
		ant.copy(todir: "${process.targetPath}/logs") {
			ant.fileset(dir: "${tomcat.tomcatDir}/logs/")
		}
	}

	@Override
	void failIfRunning() {
		tomcat.checkIfIsRunning()
	}

	Tomcat getTomcat() {
		new Tomcat(ant: ant, engineDir: serverPath)
	}

	@Override
	void startup() {
		tomcat.startup()
	}

	@Override
	void shutdown() {
		tomcat.shutdown()
	}

	@Override
	void install() {
		ant.ant(antfile: "build.xml", target: getName())
	}

	@Override
	void deploy(BetsyProcess process) {
		ant.copy(file: process.targetPackageFilePath, todir: deploymentDir)
	}

	@Override
	void onPostDeployment(BetsyProcess process) {
		ant.sequential() {
			ant.waitfor(maxwait: "100", maxwaitunit: "second") {
				 and {
					 available file: "${deploymentDir}/work/ae_temp_${process.bpelFileNameWithoutExtension}_zip/deploy.xml"
					 or {
						 resourcecontains(resource: "${tomcat.tomcatDir}/logs/bpelg.log", substring: "Deployment successful")
						 resourcecontains(resource: "${tomcat.tomcatDir}/logs/bpelg.log", substring: "Deployment failed")
					 }
				 }
			}
		}
	}

	@Override
	void buildArchives(BetsyProcess process) {
		packageBuilder.createFolderAndCopyProcessFilesToTarget(process)

		// deployment descriptor
		ant.xslt(in: process.bpelFilePath, out: "${process.targetBpelPath}/deploy.xml", style: "${getXsltPath()}/bpelg_bpel_to_deploy_xml.xsl")

        // remove unimplemented methods
        Util.computeMatchingPattern(process).each { pattern ->
            ant.copy(file:  "${process.targetBpelPath}/TestInterface.wsdl", tofile: "${process.targetTmpPath}/TestInterface.wsdl.before_removing_${pattern}")
            ant.xslt(in: "${process.targetTmpPath}/TestInterface.wsdl.before_removing_${pattern}", out: "${process.targetBpelPath}/TestInterface.wsdl", style: "$xsltPath/bpelg_prepare_wsdl.xsl", force: "yes") {
                param(name: "deletePattern", expression: pattern)
            }
        }

		// uniquify service name
		ant.replace(file: "${process.targetBpelPath}/TestInterface.wsdl", token: "TestInterfaceService", value: "${process.bpelFileNameWithoutExtension}TestInterfaceService")
		ant.replace(file: "${process.targetBpelPath}/deploy.xml", token: "TestInterfaceService", value: "${process.bpelFileNameWithoutExtension}TestInterfaceService")

		packageBuilder.replaceEndpointTokenWithValue(process)
		packageBuilder.replacePartnerTokenWithValue(process)
		packageBuilder.bpelFolderToZipFile(process)
	}

	@Override
	String getEndpointUrl(BetsyProcess process) {
		"${tomcat.tomcatUrl}/bpel-g/services/${process.bpelFileNameWithoutExtension}TestInterfaceService"
	}

}