/*
Copyright (c) 2013-2014 Fengguo Wei & Sankardas Roy, Kansas State University.        
All rights reserved. This program and the accompanying materials      
are made available under the terms of the Eclipse Public License v1.0 
which accompanies this distribution, and is available at              
http://www.eclipse.org/legal/epl-v10.html                             
*/
package org.sireum.amandroid.security.dataInjection

import org.sireum.util._
import org.sireum.jawa.MessageCenter._
import org.sireum.amandroid.appInfo.AppInfoCollector
import org.sireum.amandroid.AppCenter
import org.sireum.jawa.util.IgnoreException
import org.sireum.jawa.JawaRecord
import org.sireum.amandroid.AndroidConstants
import org.sireum.jawa.Center
import org.sireum.amandroid.appInfo.ReachableInfoCollector
import org.sireum.jawa.util.MyTimer

/**
 * @author <a href="mailto:fgwei@k-state.edu">Fengguo Wei</a>
 * @author <a href="mailto:sroy@k-state.edu">Sankardas Roy</a>
 */ 
class IntentInjectionCollector(apkUri : FileResourceUri, outputUri : FileResourceUri, timer : Option[MyTimer]) extends AppInfoCollector(apkUri, outputUri, timer) {
  private final val TITLE = "IntentInjectionCollector"
  var ra : ReachableInfoCollector = null
	def getInterestingContainers(interestingAPIs : Set[String]) = {
	  var interestingContainers : Set[JawaRecord] = Set()
    interestingAPIs.foreach{
		  api =>
		    interestingContainers ++= this.ra.getSensitiveAPIContainer(api)
		}
		if(interestingContainers.isEmpty) throw new IgnoreException
    interestingContainers
  }
  
	override def collectInfo : Unit = {
	  val manifestUri = outputUri + "/AndroidManifest.xml"
    val mfp = AppInfoCollector.analyzeManifest(manifestUri)
	  this.appPackageName = mfp.getPackageName
		this.componentInfos = mfp.getComponentInfos
		this.uses_permissions = mfp.getPermissions
		this.intentFdb = mfp.getIntentDB
		
	  val afp = AppInfoCollector.analyzeARSC(apkUri)
		val lfp = AppInfoCollector.analyzeLayouts(apkUri, mfp)
		this.layoutControls = lfp.getUserControls
		this.ra = AppInfoCollector.reachabilityAnalysis(mfp, timer)
		val callbacks = AppInfoCollector.analyzeCallback(afp, lfp, ra)
		this.callbackMethods = callbacks
		var components = isetEmpty[JawaRecord]
    mfp.getComponentInfos.foreach{
      f => 
        val record = Center.resolveRecord(f.name, Center.ResolveLevel.HIERARCHY)
        if(!record.isUnknown && record.isApplicationRecord){
	        components += record
	        val clCounter = generateEnvironment(record, if(f.exported)AndroidConstants.MAINCOMP_ENV else AndroidConstants.COMP_ENV, codeLineCounter)
	        codeLineCounter = clCounter
        }
    }
		
		AppCenter.setComponents(components)
		AppCenter.updateIntentFilterDB(this.intentFdb)
		AppCenter.setAppInfo(this)
		msg_normal(TITLE, "Entry point calculation done.")
	}
}