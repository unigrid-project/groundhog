package keeper

import (
	"hedgehog/x/hedgehog/types"
)

var _ types.QueryServer = Keeper{}
