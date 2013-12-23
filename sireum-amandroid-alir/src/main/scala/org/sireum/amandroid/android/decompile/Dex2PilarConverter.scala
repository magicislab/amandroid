package org.sireum.amandroid.android.decompile

import java.io._
import org.sireum.util._
import java.net.URI
import org.sireum.amandroid.alir.AndroidGlobalConfig

object Dex2PilarConverter {
  var dex2pilarFile = new File(System.getenv(AndroidGlobalConfig.SIREUM_HOME) + "/apps/amandroid/bin/dex2pilar")
  if(!dex2pilarFile.exists()){
    val dex2pilarDir = System.getenv(AndroidGlobalConfig.DEX2PILAR_DIR)
    dex2pilarFile = if(dex2pilarDir != null) new File(dex2pilarDir + "/dex2pilar") else throw new RuntimeException("Does not have env var: " + AndroidGlobalConfig.DEX2PILAR_DIR)
  }
  
	val dexdumputil = Util(dex2pilarFile)
	
	def convert(f : FileResourceUri) : FileResourceUri = {
	  if (f.endsWith("dex") || f.endsWith("odex")) {
      val uri = new URI(f)
        val args = ilist("/bin/bash", "-c",
          dexdumputil.dexdump.getAbsolutePath() + " -d -f -h -p " + uri.getPath())
        val clOutput = new Exec().run(200000, args, None, None)  // check last argument
        //println(clOutput) // showing command line output

       
        val t : FileResourceUri = f.substring(0, f.lastIndexOf('.')).concat(".pilar") // check t = s - .dex + .pilar
       
        t // check if little type mismatch
    } else throw new RuntimeException("Given file is not a dex file: " + f)
	}
	
}

case class Util(dexdump : File) {

  def copy(srcUri : FileResourceUri, destUri : FileResourceUri) {
      def copyFile(f : File) {
        try {
          val fin = new FileInputStream(f)
          val dest = new File(new File(new URI(destUri)), f.getName())
          val fout = new FileOutputStream(dest)
          val buffer = new Array[Byte](1024)
          var bytesRead = fin.read(buffer)
          while (bytesRead > 0) {
            fout.write(buffer, 0, bytesRead)
            bytesRead = fin.read(buffer)
          }
          fin.close
          fout.close
        } catch {
          case e : Exception =>
            e.printStackTrace()
        }
      }

    val src = new File(new URI(srcUri))
    val dest = new File(new URI(destUri))

    if (src.exists() && src.isDirectory()) {
      src.listFiles().foreach { f =>
        if (f.isFile()) {
          copyFile(f)
        }
      }
    }
  }

  def cleanDir(dirUri : FileResourceUri) {
    val dir = new File(new URI(dirUri))

    if (dir.exists)
      dir.listFiles.foreach { f =>
        if (f.isDirectory()) {
          cleanDir(f.getAbsoluteFile.toURI.toASCIIString)
        }
        f.delete()
      }
  }
}