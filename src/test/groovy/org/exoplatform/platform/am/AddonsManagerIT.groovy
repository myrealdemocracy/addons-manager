/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This file is part of eXo Platform - Add-ons Manager.
 *
 * eXo Platform - Add-ons Manager is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * eXo Platform - Add-ons Manager software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with eXo Platform - Add-ons Manager; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.platform.am

import org.exoplatform.platform.am.settings.PlatformSettings
import spock.lang.Shared
import spock.lang.Specification

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

/**
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
class AddonsManagerIT extends Specification {

  @Shared
  String testedArtifactPath = System.getProperty("testedArtifactPath")
  @Shared
  String testDataPath = System.getProperty("testDataPath")
  @Shared
  File productHome = new File(System.getProperty("integrationTestsDirPath")).listFiles().first()

  def setupSpec() {
    assertNotNull("Tested artifact path mustn't be null", testedArtifactPath)
    assertNotNull("Path to tests data mustn't be null", testDataPath)
    assertNotNull("Integration tests directory path mustn't be null", System.getProperty("integrationTestsDirPath"))
    assertTrue("Integration tests directory must be a directory",
               new File(System.getProperty("integrationTestsDirPath")).isDirectory())
    assertTrue("PLF_HOME must be a directory", productHome.isDirectory())
  }

  def "Test exit code"(String params, int expectedExitCode) {
    expect:
    println "Testing on ${productHome.name}, expecting return code ${expectedExitCode} with params \"${params}\""
    expectedExitCode == launchAddonsManager(params)

    where:
    params                                                                     | expectedExitCode
    ""                                                                         | AddonsManagerConstants.RETURN_CODE_INVALID_COMMAND_LINE_PARAMS // Without any param the program must return an error code 1
    "--help"                                                                   | AddonsManagerConstants.RETURN_CODE_OK // With --help param the program must display the help return 0
    "list"                                                                     | AddonsManagerConstants.RETURN_CODE_OK // With list param the program must display the list of available add-ons and return 0
    "list --catalog=file://${System.getProperty("testDataPath")}/catalog.json" | AddonsManagerConstants.RETURN_CODE_OK // List add-ons from another catalog [AM_CAT_02]
    "list --no-cache"                                                          | AddonsManagerConstants.RETURN_CODE_OK // List add-ons without using local cache
    "list --offline"                                                           | AddonsManagerConstants.RETURN_CODE_OK // List add-ons in offline mode (thus from data in cache)
    "list --no-cache --offline"                                                | AddonsManagerConstants.RETURN_CODE_OK // List add-ons in offline mode with no cache (thus only the local catalog is used)
    "install exo-chat-extension"                                               | AddonsManagerConstants.RETURN_CODE_OK // Install an extension
    "uninstall exo-chat-extension"                                             | AddonsManagerConstants.RETURN_CODE_OK // Uninstall an extension
    "list --snapshots"                                                         | AddonsManagerConstants.RETURN_CODE_OK // With list --snapshots param the program must display the list of available add-ons and return 0
    "install exo-sirona:1.0.0"                                                 | AddonsManagerConstants.RETURN_CODE_OK // Install another extension with a given version
    "install exo-sirona --snapshots"                                           | AddonsManagerConstants.RETURN_CODE_ADDON_ALREADY_INSTALLED // Install the same extension must fail
    "install exo-sirona --snapshots --force"                                   | AddonsManagerConstants.RETURN_CODE_OK // Install the same extension must succeed if forced
    "uninstall exo-sirona"                                                     | AddonsManagerConstants.RETURN_CODE_OK // Uninstall it
    "install exo-sirona --no-cache"                                            | AddonsManagerConstants.RETURN_CODE_OK // Install the same extension without cache must succeed
    "uninstall exo-sirona"                                                     | AddonsManagerConstants.RETURN_CODE_OK // Uninstall it
    "install exo-sirona --offline"                                             | AddonsManagerConstants.RETURN_CODE_OK // Install the same extension in offline mode must succeed
    "uninstall exo-sirona"                                                     | AddonsManagerConstants.RETURN_CODE_OK // Uninstall it
    "install unknown-addon"                                                    | AddonsManagerConstants.RETURN_CODE_ADDON_NOT_FOUND // Install unknown add-on
    "uninstall unknown-addon"                                                  | AddonsManagerConstants.RETURN_CODE_ADDON_NOT_INSTALLED // Uninstall unknown add-on

  }

  /**
   * Helper method used to execute the addons manager
   * @param params Command line parameters to pass to the addons manager
   * @return The process return code
   */
  private int launchAddonsManager(String params) {
    def commandToExecute = ["${System.getProperty('java.home')}/bin/java"]
    // If Jacoco Agent is used, let's pass it to the forked VM
    if (System.getProperty('jacocoAgent') != null) {
      commandToExecute << "${System.getProperty('jacocoAgent')}"
    }
    commandToExecute << "-D${PlatformSettings.PLATFORM_HOME_SYS_PROP}=${productHome.absolutePath}"
    commandToExecute << "-jar ${testedArtifactPath}"
    commandToExecute << params
    println "Command launched : ${commandToExecute.join(' ')}"
    Process process = commandToExecute.join(' ').execute()
    process.waitFor() // Wait for the command to finish
    // Obtain status and output
    println "return code: ${process.exitValue()}"
    println "stderr: ${process.err.text}"
    println "stdout: ${process.in.text}" // *out* from the external program is *in* for groovy
    return process.exitValue()
  }

}