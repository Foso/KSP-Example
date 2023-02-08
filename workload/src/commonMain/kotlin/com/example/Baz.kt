package com.example

@Target(AnnotationTarget.CLASS)
annotation class Provider

@Target(AnnotationTarget.CLASS)
annotation class Module


@Provider
class A {
    fun hello(){
        println("World")
    }
}

@Provider
class B


@Module
interface TestModule{
    fun getA(): A
    fun getB(): B
}


/*
class MyModule : TestModule{
    override fun getA(): A = A()
    override fun getB(): B = B()
}
 */
