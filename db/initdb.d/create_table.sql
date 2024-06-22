-- Members 테이블 생성
CREATE TABLE members (
                       email VARCHAR(255) PRIMARY KEY,
                       name VARCHAR(255),
                       password VARCHAR(255),
                       phone VARCHAR(255),
                       address VARCHAR(255)
);

-- Orders 테이블 생성
CREATE TABLE Orders (
                        orderKey INT PRIMARY KEY,
                        userID VARCHAR(255),
                        orderStatus VARCHAR(255),
                        orderAt TIMESTAMP,
                        FOREIGN KEY (userID) REFERENCES members(email)
);

-- OrderDetail 테이블 생성
CREATE TABLE OrderDetail (
                             orderDetailKey INT PRIMARY KEY,
                             orderKey INT,
                             productID INT,
                             orderPrice INT,
                             productCount INT,
                             FOREIGN KEY (orderKey) REFERENCES Orders(orderKey),
                             FOREIGN KEY (productID) REFERENCES Products(productID)
);

-- Products 테이블 생성
CREATE TABLE Products (
                          productID INT PRIMARY KEY,
                          productName VARCHAR(255),
                          price INT,
                          stock INT
);

-- WishList 테이블 생성
CREATE TABLE WishList (
                          wishID INT PRIMARY KEY,
                          userID VARCHAR(255),
                          productID INT,
                          addAt TIMESTAMP,
                          FOREIGN KEY (userID) REFERENCES members(email),
                          FOREIGN KEY (productID) REFERENCES Products(productID)
);
