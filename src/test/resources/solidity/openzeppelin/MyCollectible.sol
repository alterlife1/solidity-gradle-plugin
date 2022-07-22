// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.8.0;

import "./ITestERC721.sol";


contract MyCollectible is ITestERC721 {
    constructor() ITestERC721("MyCollectible", "MCO") public {
    }
}
