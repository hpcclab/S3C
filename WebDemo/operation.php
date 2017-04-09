<td>
  <a href="edit.php?file-to-edit=<?php echo $value_1[3];?>" title="Download Plain Text">
    <span class="glyphicon glyphicon-pencil"></span>
  </a>
  <a id="<?php echo $value_1[3];?>" class = "removeLink__" title="Remove this file">
    <span class="glyphicon glyphicon-remove" id="<?php echo $value_1[3];?>">
      <form action="home.php" method="post" id="removeForm<?php echo $value_1[3];?>" onsubmit="return somefunction('removeForm<?php echo $value_1[3];?>');">
        <input type="hidden" name="fileToRemove" value="<?php echo $value_1[3];?>"/>
      </form>
    </span>
  </a>
  <a href="../cloud/cloudserver/storage/<?php echo $value_1[3];?>" title="Download encrypted file">
    <span class="glyphicon glyphicon-cloud-download"></span>
  </a>
  <a href="../cloud/cloudserver/watch/<?php echo str_replace('.txt','.key',$value_1[3]);?>" title="Download key file">
    <span class="glyphicon glyphicon-compressed"></span>
  </a>
</td>
