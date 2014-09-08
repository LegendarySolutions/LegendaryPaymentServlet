DROP TABLE IF EXISTS orders;
CREATE TABLE orders(id bigint auto_increment, orderId VARCHAR(255), product VARCHAR(255), amount INT, status varchar(255), email VARCHAR(255));
INSERT INTO orders(orderId, product, amount, status, email) VALUES ('10001', 'Clean Code book', 3000, 'NEW', 'foo@example.com');
INSERT INTO orders(orderId, product, amount, status, email) VALUES ('10002', 'Working Effectively with Legacy Code', 4200, 'NEW', 'bar@example.com');
INSERT INTO orders(orderId, product, amount, status, email) VALUES ('10003', 'TDD by example', 2500, 'NEW', 'tdd@example.com');
