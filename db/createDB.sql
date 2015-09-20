DROP TABLE `Log`;
DROP TABLE `Xref`;
CREATE TABLE `Log` (
  `Id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Station` varchar(16) NOT NULL DEFAULT '',
  `When` timestamp NOT NULL,
  `Temperature` decimal(4,1) NOT NULL DEFAULT '0',
  `Humidity` int unsigned NOT NULL DEFAULT '0',
  `DewPoint` decimal(4,1) NOT NULL,
  `WindDir` varchar(16) NOT NULL DEFAULT '0',
  `WindDirDeg` decimal(4,1) unsigned DEFAULT '0',
  `WindSpeed` decimal(4,1) NOT NULL DEFAULT '0',
  `WindGust` decimal(4,1) DEFAULT NULL,
  `Pressure` decimal(4,1) DEFAULT NULL,
  `RainRate` decimal(4,1) DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `ak1` (`Station`,`When`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

CREATE TABLE `Xref` (
  `Id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Station` varchar(16) NOT NULL DEFAULT '',
  `Name` varchar(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`Id`),
  KEY `ak1` (`Station`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

insert into Xref (`Station`,`Name`) 
       values ('KNJFLEMI11','Flemington, NJ')
             ,('KNVRENO55','Reno NV')
             ,('KPAWYSOX2','Ulster, PA')
             ,('IONTARIO243','Deep River, Ont')
             ,('KFLNEPTU2','Neptune Beach, FL')
             ,('MAT122','Durango, CO')
             ,('MAT067','Newport Beach, CA');
