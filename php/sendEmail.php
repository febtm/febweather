<?php

$subject = $_POST['subject'];
$message = $_POST['message'];

$headers = 'From: Febulous <fmt.febulous@gmail.com>' . "\r\n" .
        'Reply-To: Febulous <fmt.febulous@gmail.com>' . "\r\n" .
        'X-Mailer: PHP/' . phpversion();

$receiver_email = "fmt.febulous@gmail.com";

if(mail($receiver_email, $subject, $message, $headers))
	echo "Your Message has been sent : Kindly wait until our Team responds to your Message !";

else
	echo "Sending Email Failed !";
 
?>