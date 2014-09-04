DROP TABLE IF EXISTS payments;
CREATE TABLE payments(id bigint auto_increment, status varchar(255));
INSERT INTO payments(status) VALUES ('NEW');
INSERT INTO payments(status) VALUES ('PAID');
