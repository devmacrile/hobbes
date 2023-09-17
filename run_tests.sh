for file in resources/testfiles/*/*.tiger
do
 echo $file
 java -jar hobbes_bin/tigerc.jar -f $file -i
 echo '--------------------'
done
