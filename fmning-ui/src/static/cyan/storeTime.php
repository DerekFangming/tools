<?php
$myfile = fopen("time.txt", "w") or die("Unable to open file!");
$timestamp = strtotime('10:09') + 60*60;
$txt = date("Y-m-d H:i:s",strtotime('+1 hour')); 
fwrite($myfile, $txt);
fclose($myfile);
?>