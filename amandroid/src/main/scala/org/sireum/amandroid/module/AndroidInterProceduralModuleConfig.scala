package org.sireum.amandroid.module

import org.sireum.pipeline._
import org.sireum.option.PipelineMode
import org.sireum.pipeline.gen.ModuleGenerator
import org.sireum.util._
import org.sireum.core.module.AlirIntraProcedural
import org.sireum.pilar.ast.{LocationDecl, CatchClause}
import org.sireum.alir._
import org.sireum.pilar.symbol._
import org.sireum.amandroid.interProcedural.callGraph.CallGraph
import org.sireum.amandroid.android.cache.AndroidCacheFile
import org.sireum.amandroid.android.appInfo.PrepareApp
import org.sireum.amandroid.interProcedural.callGraph.CGNode

/*
Copyright (c) 2012-2013 Sankardas Roy & Fengguo Wei, Kansas State University.        
All rights reserved. This program and the accompanying materials      
are made available under the terms of the Eclipse Public License v1.0 
which accompanies this distribution, and is available at              
http://www.eclipse.org/legal/epl-v10.html                             
*/

object AndroidInterProcedural {
  type VirtualLabel = String
  type CG = CallGraph[CGNode]
    
  final case class AndroidInterAnalysisResult(
    callGraph : AndroidInterProcedural.CG)
        
}

/**
 * @author <a href="mailto:fgwei@k-state.edu">Fengguo Wei</a>
 * @author <a href="mailto:sroy@k-state.edu">Sankar Roy</a>
 * @author <a href="mailto:robby@k-state.edu">Robby</a>
 */

case class AndroidInterProcedural(
  title : String = "Android InterProcedural Module",
  
  @Input
  parallel : Boolean,
  
  @Input 
  symbolTable : SymbolTable,
  
  @Input
  shouldBuildCallGraph : Boolean = true,
  
  @Input
  appInfoOpt : scala.Option[PrepareApp] = None,
  
  @Input
  intraResult : MMap[ResourceUri, AndroidIntraProcedural.AndroidIntraAnalysisResult],
    
  @Output interResult : AndroidInterProcedural.AndroidInterAnalysisResult
  
)
 

case class CG(
  title : String = "System Control Flow Graph with OFA Builder",
  
  @Input 
  cfgs : MMap[ResourceUri, ControlFlowGraph[String]],
  
  @Input 
  rdas : MMap[ResourceUri, ReachingDefinitionAnalysis.Result],
    
  @Input
  procedureSymbolTables : Seq[ProcedureSymbolTable],
  
  @Input
  appInfoOpt : scala.Option[PrepareApp],
  
  // for test now. Later will change it.
  @Output
  @Produce
  callGraph : AndroidInterProcedural.CG)
  
  
/**
 * @author <a href="mailto:fgwei@k-state.edu">Fengguo Wei</a>
 */
  
object AndroidInterProceduralModuleBuild {
  def main(args : Array[String]) {
    val opt = PipelineMode()
    opt.classNames = Array(      
        AndroidInterProcedural.getClass().getName().dropRight(1),
        CG.getClass().getName().dropRight(1)
        )
    opt.dir = "./src/main/scala/org/sireum/amandroid/module"
    opt.genClassName = "AndroidInterProceduralModuleCore"

    ModuleGenerator.run(opt)
  }
}