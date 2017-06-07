import groovy.xml.*

def blueProjectsjobDefn = 	[
					"Blue Projects"	:	// Each Element is a Entry with Key being the project Name and Value being the Git URL
								[
									"springboot-companies"     	: 	"https://github.com/dogbonnahNB/springboot.git",

								]

				]

def redProjectsjobDefn = 	[
					"Red Projects"	:	// Each Element is a Entry with Key being the project Name and Value being the Git URL
								[
									"DavidIMS"     	: 	"https://github.com/dogbonnahNB/DavidIMS.git",

								]

				]


// Don't change anything below unless you know what you doing
blueProjectsjobDefn.each { entry ->
  println "View  " + entry.key
	entry.value.each { job ->
        println "Job  " + job.key
		jobName = job.key;
		jobVCS = job.value;
		projectType = 'blueProject';
		buildMultiBranchJob(jobName, jobVCS, projectType)
	}
  listView("${entry.key}") {
    jobs {
      entry.value.each { job ->
        name("${job.key}")
      }
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
  }

}

redProjectsjobDefn.each { entry ->
  println "View  " + entry.key
	entry.value.each { job ->
        println "Job  " + job.key
		jobName = job.key;
		jobVCS = job.value;
		projectType = 'redProject';
		buildMultiBranchJob(jobName, jobVCS, projectType)
	}
  listView("${entry.key}") {
    jobs {
      entry.value.each { job ->
        name("${job.key}")
      }
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
  }

}



// Define method to build the job
def buildMultiBranchJob(jobName, jobVCS, projectType) {

	def testUsers = ['testuser', 'newuser']
	def testBlueProjectsPermissionsList = [ 'hudson.model.Item.Workspace', 'hudson.model.Item.Read', 'hudson.model.Item.Configure', 'hudson.model.Item.Delete', 'hudson.model.Item.Cancel', 'hudson.model.Item.Move', 'hudson.model.Item.Discover', 'hudson.model.Item.Create']
	def testRedProjectsPermissionsList = [ 'hudson.model.Item.Workspace', 'hudson.model.Item.Read', 'hudson.model.Item.Build', 'hudson.model.Item.Delete', 'hudson.model.Item.Cancel', 'hudson.model.Item.Move', 'hudson.model.Item.Discover', 'hudson.model.Item.Create']
	def PermissionsList = []
	int outerIndex = 0
	int innerIndex = 0
	def index = 0

	if(projectType.equals('blueProject')) {
		while(outerIndex < testBlueProjectsPermissionsList.size())
		{
			String tempString = testBlueProjectsPermissionsList.get(outerIndex)
			while(innerIndex < testUsers.size())
			{
				permString = tempString + ":" + testUsers.get(innerIndex)
				PermissionsList.add(permString)
				innerIndex++
			}

			innerIndex = 0
			outerIndex++
		}
	} else {
		while(outerIndex < testRedProjectsPermissionsList.size())
		{
			String tempString = testRedProjectsPermissionsList.get(outerIndex)
			while(innerIndex < testUsers.size())
			{
				permString = tempString + ":" + testUsers.get(innerIndex)
				PermissionsList.add(permString)
				innerIndex++
			}

			innerIndex = 0
			outerIndex++
		}
	}

	while(index < PermissionsList.size())
	{
		String tempString = PermissionsList.get(index)
		println tempString
		index++
	}

	// Create job
	multibranchPipelineJob(jobName) {
		// Define source
		branchSources {
			branchSourceNodes << new NodeBuilder().'jenkins.branch.BranchSource' {
				source(class: 'jenkins.plugins.git.GitSCMSource') {
					id(UUID.randomUUID())
					remote(jobVCS)
					includes('*')
					excludes('')
					ignoreOnPushNotifications(false)
					extensions {
						localBranch(class: "hudson.plugins.git.extensions.impl.LocalBranch") {
							localBranch('**')
						}
					}
				}
			}
		} // End source

		// Triggers
		triggers {

		  cron('H/15 * * * *')

		} // End of Triggers

		configure { node ->
			node / 'properties' / 'com.cloudbees.hudson.plugins.folder.properties.AuthorizationMatrixProperty' {
				for(int i = 0; i < PermissionsList.size(); i++) {
					String temp = PermissionsList.getAt(i)
					permission(temp)
				}
			}
		}

    configure { node ->
      node / 'properties' << 'org.jenkinsci.plugins.workflow.libs.FolderLibraries' (plugin: 'workflow-cps-global-lib@2.4'){
        libraries {
          'org.jenkinsci.plugins.workflow.libs.LibraryConfiguration' {
            name('common')
              retriever(class: 'org.jenkinsci.plugins.workflow.libs.SCMRetriever') {
                scm(class: 'hudson.plugins.git.GitSCM', plugin: 'git@3.0.0') {
                  configVersion('2')
                    userRemoteConfigs {
                      'hudson.plugins.git.UserRemoteConfig' {
                        url("https://github.com/dogbonnahNB/springboot.git")
                      }
                    }
                    branches {
                      'hudson.plugins.git.BranchSpec' {
                        name('*/master')
                      }
                    }
                    doGenerateSubmoduleConfigurations('false')
                    submoduleCfg(class: 'list')
                    extensions
                }
              }
              defaultVersion('master')
              implicit('false')
              allowVersionOverride('true')
          }

        }
      }
    }

		// How Many Items in the history
		orphanedItemStrategy {
		discardOldItems {
			daysToKeep(0)
			numToKeep(0)
			}
		} // End Orphaned
	} // End Creating
} // End Method
