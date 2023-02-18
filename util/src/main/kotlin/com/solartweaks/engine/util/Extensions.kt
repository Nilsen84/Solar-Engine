package com.solartweaks.engine.util

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.TraceClassVisitor
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.lang.instrument.Instrumentation
import kotlin.reflect.KClass

/**
 * Defines the ASM API level
 */
const val asmAPI = ASM9

/**
 * Yields the internal name of this [Class]
 */
val Class<*>.internalName get() = name.replace('.', '/')

/**
 * Yields the internal name of this [KClass]
 */
val KClass<*>.internalName get() = java.internalName

/**
 * Yields the internal name of [T]
 */
inline fun <reified T> internalNameOf() = T::class.internalName

/**
 * Yields the resource name of this [Class]
 */
val Class<*>.resourceName get() = "${internalName}.class"

/**
 * Yields the resource name of this [KClass]
 */
val KClass<*>.resourceName get() = java.resourceName

/**
 * Yields the resource name of [T]
 */
inline fun <reified T> resourceNameOf() = T::class.resourceName

/**
 * Converts a class name represented by this [String] to a resource name
 */
val String.classResourceName get() = "${replace('.', '/')}.class"

/**
 * Gets the first occurrence of a method named [name] in this [ClassNode]
 */
fun ClassNode.methodByName(name: String) = methods.find { it.name == name }

/**
 * Finds the static initializer of this [ClassNode]
 */
val ClassNode.initializer get() = methodByName("<clinit>")

/**
 * Gets the first occurrence of a field named [name] in this [ClassNode]
 */
fun ClassNode.fieldByName(name: String) = fields.find { it.name == name }

/**
 * Finds all constant pool values of this [ClassNode]
 */
val ClassNode.constants get() = methods.flatMap { it.constants } + fields.mapNotNull { it.value }

/**
 * Finds all strings of this [ClassNode]
 */
val ClassNode.strings get() = constants.filterIsInstance<String>()

/**
 * Finds all constant pool values of this [MethodNode]
 */
val MethodNode.constants
    get() = instructions.filterIsInstance<LdcInsnNode>().map { it.cst } +
            instructions.filterIsInstance<InvokeDynamicInsnNode>().flatMap { it.bsmArgs.asIterable() }

/**
 * Finds all strings of this [MethodNode]
 */
val MethodNode.strings get() = constants.filterIsInstance<String>()

/**
 * Checks if this [ClassNode] has a constant value of [cst]
 */
fun ClassNode.hasConstant(cst: Any) = methods.any { it.hasConstant(cst) }

/**
 * Checks if this [MethodNode] has a constant value of [cst]
 */
fun MethodNode.hasConstant(cst: Any) = constants.contains(cst)

/**
 * Finds all method invocations (excluding `invokedynamic`) in this [MethodNode]
 */
val MethodNode.calls get() = instructions.filterIsInstance<MethodInsnNode>()

/**
 * Checks if this [MethodNode] calls a method that matches [matcher]
 */
fun MethodNode.calls(matcher: (MethodInsnNode) -> Boolean) = calls.any(matcher)

/**
 * Checks if this [MethodNode] calls a method that is named [name]
 */
fun MethodNode.callsNamed(name: String) = calls { it.name == name }

/**
 * Finds all field references in this [MethodNode]
 */
val MethodNode.references get() = instructions.filterIsInstance<FieldInsnNode>()

/**
 * Checks if this [MethodNode] references a field that matches [matcher]
 */
fun MethodNode.references(matcher: (FieldInsnNode) -> Boolean) = references.any(matcher)

/**
 * Checks if this [MethodNode] references a field that is named [name]
 */
fun MethodNode.referencesNamed(name: String) = references { it.name == name }

/**
 * Checks if this [MethodNode] returns a value described by the passed descriptor [toReturn]
 */
fun MethodNode.returns(toReturn: String) = Type.getReturnType(desc).descriptor == toReturn

/**
 * All package names/class name prefixes to ignore when finding app/Minecraft related classes
 */
val systemClasses = listOf(
    "java.", "sun.", "jdk.", "com.sun.management.",
    "com.solartweaks.", "kotlin.", "kotlinx.", "org.objectweb.",
)

/**
 * Returns if a binary classname is a system class
 */
fun isSystemClass(className: String) = systemClasses.any { className.startsWith(it) }

/**
 * Returns all loaded classes by this JVM given by this [Instrumentation] that:
 * - Are not loaded by the Bootstrap ClassLoader
 * - Are not Array Classes
 * - Are not a system class
 *
 * @see systemClasses
 */
fun Instrumentation.getAppClasses(): List<Class<*>> = allLoadedClasses.filter { c ->
    c.classLoader != null &&
            !c.isArray &&
            !isSystemClass(c.name) &&
            !c.name.contains("\$\$Lambda")
}

/**
 * Whether the method is `public`
 */
val MethodNode.isPublic get() = access and ACC_PUBLIC != 0

/**
 * Whether the method is `private`
 */
val MethodNode.isPrivate get() = access and ACC_PRIVATE != 0

/**
 * Whether the method is `protected`
 */
val MethodNode.isProtected get() = access and ACC_PROTECTED != 0

/**
 * Whether the method is `static`
 */
val MethodNode.isStatic get() = access and ACC_STATIC != 0

/**
 * Whether the method is `final`
 */
val MethodNode.isFinal get() = access and ACC_FINAL != 0

/**
 * Whether the method is `abstract`
 */
val MethodNode.isAbstract get() = access and ACC_ABSTRACT != 0

/**
 * Whether the method is a constructor
 */
val MethodNode.isConstructor get() = name == "<init>"

/**
 * Whether the field is `public`
 */
val FieldNode.isPublic get() = access and ACC_PUBLIC != 0

/**
 * Whether the field is `private`
 */
val FieldNode.isPrivate get() = access and ACC_PRIVATE != 0

/**
 * Whether the field is `static`
 */
val FieldNode.isStatic get() = access and ACC_STATIC != 0

/**
 * Whether the field is `final`
 */
val FieldNode.isFinal get() = access and ACC_FINAL != 0

/**
 * Whether the class is not a class, but an interface
 */
val ClassNode.isInterface get() = access and ACC_INTERFACE != 0

/**
 * Whether the class is `public`
 */
val ClassNode.isPublic get() = access and ACC_PUBLIC != 0

/**
 * Whether the class is `private`
 */
val ClassNode.isPrivate get() = access and ACC_PRIVATE != 0

/**
 * Whether the class is `static`
 */
val ClassNode.isStatic get() = access and ACC_STATIC != 0

/**
 * Whether the class is `final`
 */
val ClassNode.isFinal get() = access and ACC_FINAL != 0

/**
 * Whether the class is `abstract`
 */
val ClassNode.isAbstract get() = access and ACC_ABSTRACT != 0

/**
 * Yields a [Type] representing this [Class]
 */
val Class<*>.asmType: Type get() = Type.getType(this)

/**
 * Yields a [Type] representing this [KClass]
 */
val KClass<*>.asmType get() = java.asmType

/**
 * Returns a [Type] representing [T]
 */
inline fun <reified T> asmTypeOf() = T::class.java.asmType

/**
 * Returns a [Type] representing [T], where [T] is primitive
 */
inline fun <reified T : Any> primitiveTypeOf() =
    (T::class.javaPrimitiveType
        ?: error("T is not a primitive type!")).asmType

/**
 * Returns a [Type] represented by the class [internalName]
 */
fun asmTypeOf(internalName: String) = Type.getObjectType(internalName)

/**
 * Yields a [Type] represented by this [ClassNode]
 */
val ClassNode.type: Type get() = Type.getObjectType(name)

/**
 * Returns all methods of this [ClassNode] as [MethodData]
 */
val ClassNode.methodData get() = methods.map { MethodData(this, it) }

/**
 * Returns all fields of this [ClassNode] as [FieldData]
 */
val ClassNode.fieldData get() = fields.map { FieldData(this, it) }

/**
 * Determines if a [Type] is primitive
 */
val Type.isPrimitive get() = sort !in listOf(Type.ARRAY, Type.OBJECT, Type.METHOD)

/**
 * Shorthand for loading classes with a given [ClassLoader]
 */
fun ClassLoader.forName(name: String, load: Boolean = false): Class<*> = Class.forName(name, load, this)

/**
 * Returns the instruction that loads the stub value of a given [Type]
 * That is, it returns the default value the jvm would give a field with said [Type]
 */
val Type.stubLoadInsn
    get() = when (sort) {
        Type.VOID -> NOP
        Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.CHAR, Type.INT -> ICONST_0
        Type.FLOAT -> FCONST_0
        Type.DOUBLE -> DCONST_0
        Type.LONG -> LCONST_0
        Type.OBJECT, Type.ARRAY -> ACONST_NULL
        else -> error("Invalid non-value type")
    }

/**
 * Finds the constructor of a [ClassNode]
 */
val ClassNode.constructor get() = methods.find { it.name == "<init>" }

/**
 * Finds the static initializer of a [ClassNode]
 */
val ClassNode.staticInit get() = methods.find { it.name == "<clinit>" }

/**
 * Gets the argument types of a [MethodNode]
 */
val MethodNode.arguments: Array<Type> get() = Type.getArgumentTypes(desc)

/**
 * Gets the return type of a [MethodNode]
 */
val MethodNode.returnType: Type get() = Type.getReturnType(desc)

fun ClassNode.debug(): String {
    val bout = ByteArrayOutputStream()
    accept(TraceClassVisitor(null, PrintWriter(bout)))
    return bout.toByteArray().decodeToString()
}

inline fun <reified T : AbstractInsnNode> AbstractInsnNode.next(p: (T) -> Boolean): T? {
    var insn = next

    while (insn != null) {
        if (insn is T && p(insn)) return insn
        insn = insn.next
    }

    return null
}