$jdkPath = "$env:USERPROFILE\.jdks\temurin-17"
if (Test-Path "$jdkPath\bin\java.exe") {
    $env:JAVA_HOME = $jdkPath
}
$mavenHome = "$env:USERPROFILE\AppData\Local\Programs\apache-maven-3.9.6"
$env:Path = "$mavenHome\bin;$env:JAVA_HOME\bin;$env:Path"

& "$mavenHome\bin\mvn.cmd" $args
