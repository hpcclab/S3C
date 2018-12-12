<?php

$filename = "rfc600.txt";
$query_1 = "java -jar S3Client.jar -d rfc61.txt";
exec($query_1, $out);
//if(!$ret){
//    print_r($out);
//}
var_dump($out);
?>