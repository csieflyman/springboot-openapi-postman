CREATE TABLE `user` (
`id` int unsigned NOT NULL AUTO_INCREMENT,
`name` varchar(20) NOT NULL,
`gender` varchar(20) NOT NULL,
`age` SMALLINT NOT NULL DEFAULT '0',
`is_member` tinyint(1) unsigned NOT NULL DEFAULT '1',
`create_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`update_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
);

CREATE TABLE `course` (
`id` int unsigned NOT NULL AUTO_INCREMENT,
`name` varchar(20) NOT NULL,
`create_at` timestamp DEFAULT CURRENT_TIMESTAMP,
`update_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
);
