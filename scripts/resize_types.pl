#!/usr/bin/perl
use strict;
use warnings;
my @types = ("F","D","W","G","C","M","R","P","N","L","Y");
foreach my $t (@types) {
  system("magick",$t.".png","-resize",$ARGV[0],$t.$ARGV[0].".png");
}

