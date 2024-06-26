CREATE TABLE IF NOT EXISTS members (
                                        email VARCHAR(255) NOT NULL,
                                        username VARCHAR(255),
                                        password VARCHAR(255),
                                        phone VARCHAR(255),
                                        address VARCHAR(255),
                                        role VARCHAR(255),
                                        PRIMARY KEY (email)
);

CREATE TABLE IF NOT EXISTS products (
                                        productID INT AUTO_INCREMENT PRIMARY KEY,
                                        productName VARCHAR(255),
                                        price INT,
                                        stock INT,
                                        likecount INT
);

CREATE TABLE IF NOT EXISTS likes (
                                        likesid BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        productID INT,
                                        email VARCHAR(255),
                                        FOREIGN KEY (email) REFERENCES Members(email),
                                        FOREIGN KEY (productID) REFERENCES products(productID)
);

CREATE TABLE IF NOT EXISTS wishlist (
                                     wishid BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(255),
                                     productID INT,
                                     addAt timestamp
);
#
# -- orders 테이블 생성
# CREATE TABLE orders (
#                         orderKey INT AUTO_INCREMENT PRIMARY KEY,
#                         email VARCHAR(255),
#                         orderStatus VARCHAR(255),
#                         orderAt TIMESTAMP
# );
#
# -- orderdetail 테이블 생성
# CREATE TABLE orderdetail (
#                              orderDetailKey INT AUTO_INCREMENT PRIMARY KEY,
#                              orderKey INT,
#                              productID INT,
#                              orderPrice INT,
#                              productCount INT
# );
