<?php
 $dir = './data/inputEncrypted';

 // create new directory with 744 permissions if it does not exist yet
 // owner will be the user/group the PHP script is run under
// if ( !file_exists($dir) ) {
  //   $oldmask = umask(0);  // helpful when used in linux server  
  //   self::deleteDir ($dir, 777);
// }

rmdir($dir)

