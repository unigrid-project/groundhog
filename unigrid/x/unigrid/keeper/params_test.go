package keeper_test

import (
	"testing"

	"github.com/stretchr/testify/require"
	testkeeper "unigrid/testutil/keeper"
	"unigrid/x/unigrid/types"
)

func TestGetParams(t *testing.T) {
	k, ctx := testkeeper.UnigridKeeper(t)
	params := types.DefaultParams()

	k.SetParams(ctx, params)

	require.EqualValues(t, params, k.GetParams(ctx))
}
