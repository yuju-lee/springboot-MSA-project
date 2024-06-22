CREATE TABLE IF NOT EXISTS members (
                                       email VARCHAR(255) NOT NULL,
                                       username VARCHAR(255),
                                       password VARCHAR(255),
                                       phone VARCHAR(255),
                                       address VARCHAR(255),
                                       role VARCHAR(255),
                                       PRIMARY KEY (email)
);
