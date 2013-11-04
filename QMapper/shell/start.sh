baseDirForScriptSelf=$(cd "$(dirname "$0")"; pwd)
echo "Static Dir is: $1"
java -jar $baseDirForScriptSelf/../QMapper-0.0.1-SNAPSHOT.jar $baseDirForScriptSelf/../site