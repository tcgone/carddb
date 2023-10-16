#!/usr/bin/perl
# scans.pl
# Author: axpendix@hotmail.com, Version: 20180710
# A script to batch process images. Auto applies png_to_jpg, strip, resize
# Usage: ../scans.pl folders

use strict;
use warnings;
use Data::Dumper qw(Dumper);
use feature 'say';

use File::Find;
use File::Temp ();
use File::Path qw(make_path);

use File::Basename;
my $dirname = dirname(__FILE__) . "/public";

sub process_file {
	my ($file) = @_;
	unless($file =~ m/\.jpg$/ || $file =~ m/\.png$/){
		return;
	}
	print "$file Processing\r";
	my $next = $file;
	my $tmp = File::Temp->new( UNLINK => 1, SUFFIX => '.jpg' );
	# convert to jpg
	my $file_jpg = $file;
	$file_jpg =~ s/png/jpg/g;
	if(index($next,"png")){
		print "\r$file Converting to jpg\e[K";
		system("magick",$next,$tmp);
		$next=$tmp;
	}
	# strip
	$tmp = File::Temp->new( UNLINK => 1, SUFFIX => '.jpg' );
	print "\r$file Stripping\e[K";
	system("magick",$next,"-strip","-interlace","Plane","-quality","80%",$tmp);
	$next=$tmp;
	print "\r$file Resizing 0\e[K";
	my $l_out = $dirname."/l/".dirname($file);
	my $m_out = $dirname."/m/".dirname($file);
	make_path($l_out, $m_out);
	$l_out .= "/" . basename($file_jpg);
	$m_out .= "/" . basename($file_jpg);
	# say $l_out;
	# say $m_out;
	print "\r$file Resizing 1\e[K";
	system("magick",$next,"-resize","450",$l_out);
	print "\r$file Resizing 2\e[K";
	system("magick",$next,"-resize","225",$m_out);
	print "\r.\e[K";
}

sub search_dir {
	my ($dir) = @_;
	unless ( -d $dir ) {
		process_file($dir);
		return;
	}	
	my $dh; # handle
	opendir ($dh, $dir);
	print "Directory: $dir\n";
	my @FILES = grep { $_ ne '.' && $_ ne '..' } readdir($dh);
	foreach my $file (@FILES) {
		# next if($file =~ /^\.$/);
		# next if($file =~ /^\.\.$/);
		my $path = "$dir/$file";
		search_dir ($path);
	}
	closedir ($dh);
}

if(scalar(@ARGV)==0){
	die "Usage: $0 file1[ file2 file3 ...]\n";
}

my @dirs=@ARGV;
foreach my $dir (@dirs) {
	search_dir($dir);
}
