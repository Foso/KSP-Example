import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStreamWriter

class TestProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val annotatedProviders =
            resolver.getSymbolsWithAnnotation("com.example.Provider").toList().filterIsInstance<KSClassDeclaration>()

        val annotatedModules =
            resolver.getSymbolsWithAnnotation("com.example.Module").toList().filterIsInstance<KSClassDeclaration>()

        annotatedModules.forEach { moduleClassDecl ->

            val moduleName = moduleClassDecl.simpleName.asString()
            val modulePackageName = moduleClassDecl.packageName.asString()

            val imports = mutableSetOf<String>()
            imports.add(moduleClassDecl.qualifiedName!!.asString())

            /**
             * Create the source code for the functions
             */
            val functionSource = moduleClassDecl.getDeclaredFunctions().toList().joinToString("\n") { funcDecl ->

                val funcName = funcDecl.simpleName.asString()
                val returnType = funcDecl.returnType?.resolve()

                if (returnType == null || returnType.isAssignableFrom(resolver.builtIns.unitType)) {
                    logger.error("Function needs return type", funcDecl)
                } else {
                    imports.add(returnType.declaration.qualifiedName!!.asString())
                }

                val provider = annotatedProviders.firstOrNull { providerClass ->
                    val classKsType = providerClass.asStarProjectedType()
                    returnType!!.isAssignableFrom(classKsType)
                }

                if (provider == null) {
                    logger.error("No provider found", funcDecl)
                }

                val providerName = provider!!.simpleName.asString()

                // override fun getA() : A = A()
                "override fun ${funcName}() : ${returnType.toString()} = ${providerName}()"
            }

            val fileSource = """
package $modulePackageName
${imports.joinToString("\n") { "import $it" }}
class My${moduleName} : ${moduleName}{
$functionSource
}
            """.trimIndent()

            codeGenerator.createNewFile(
                dependencies = Dependencies(
                    aggregating = false,
                    sources = (annotatedModules + annotatedProviders).mapNotNull { it.containingFile }.toTypedArray()
                ),
                packageName = modulePackageName,
                fileName = "My${moduleName}",
                extensionName = "kt"
            ).use { output ->
                OutputStreamWriter(output).use { writer ->
                    writer.write(fileSource)
                }
            }


        }
        return emptyList()
    }
}

class TestProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return TestProcessor(environment.codeGenerator, environment.logger)
    }
}
